package boris.company.voicemovie.service.processor;

import boris.company.voicemovie.exception.TaskNotFoundException;
import boris.company.voicemovie.model.MovieRequestTask;
import boris.company.voicemovie.repository.MovieRequestTaskRepository;
import boris.company.voicemovie.retry.RetryableErrorClassifier;
import boris.company.voicemovie.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieRequestTaskProcessor {

    private final MovieRequestTaskRepository repository;
    private final AIService aiService;
    private final MovieSearchService movieSearchService;
    private final MovieTaskResultService resultService;
    private final TranscriptionService transcriptionService;
    private final MovieTaskStateService stateService;
    private final RetryableErrorClassifier retryableErrorClassifier;
    @Value("${scheduler.movie-request.batch-size}")
    private int batchSize;
    @Value("${scheduler.movie-request.processing-timeout-seconds}")
    private long processingTimeoutSeconds;



    public void process() {

        Instant expiredBefore = Instant.now().minusSeconds(processingTimeoutSeconds);

        var tasks = repository.findAvailableTasks(batchSize,
                                        expiredBefore);

        for (var task : tasks) {

            try {
                Instant now = Instant.now();
                int lockedTasksCount = repository.lockTask(
                        task.getId(),
                        now,
                        now.minusSeconds(processingTimeoutSeconds)
                );
                if (lockedTasksCount == 0) {
                    continue;
                }
                processTask(task.getId());
            } catch (RuntimeException e) {
                log.error("Не удалось обработать задачу {}", task.getId(), e);
            }
        }
    }

    private void processTask(Long taskId) {
        while (true) {
            MovieRequestTask task = repository.findById(taskId)
                    .orElseThrow(() -> new TaskNotFoundException(taskId));

            try {
                switch (task.getStatus()) {
                    case PENDING ->
                            stateService.startFirstStage(taskId);

                    case TRANSCRIBING ->
                            processTranscription(task);

                    case EXTRACTING_CRITERIA ->
                            processCriteriaExtraction(task);

                    case SEARCHING_MOVIES -> {
                        processMovieSearch(task);
                        return;
                    }

                    case COMPLETED, FAILED -> {
                        return;
                    }
                }
            } catch (RuntimeException e) {
                handleTaskError(task, e);
                return;
            }
        }
    }

    private void processTranscription(MovieRequestTask task) {
        var result = transcriptionService.transcribe(
                task.getFileReference()
        );

        stateService.completeTranscription(task.getId(), result);
    }

    private void processCriteriaExtraction(MovieRequestTask task) {
        var criteria = aiService.extractCriteria(
                task.getInputText()
        );

        stateService.completeCriteriaExtraction(task.getId(), criteria);
    }

    private void processMovieSearch(MovieRequestTask task) {
        var movies = movieSearchService.search(
                task.getSearchCriteria()
        );

        resultService.completeTask(task.getId(), movies);
    }

    private void handleTaskError(MovieRequestTask task, RuntimeException e) {
        log.error(
                "Ошибка обработки задачи {} на стадии {}",
                task.getId(),
                task.getStatus(),
                e
        );

        stateService.retryOrFail(
                task.getId(),
                "Не удалось выполнить стадию " + task.getStatus(),
                retryableErrorClassifier.isRetryable(e)
        );
    }

}