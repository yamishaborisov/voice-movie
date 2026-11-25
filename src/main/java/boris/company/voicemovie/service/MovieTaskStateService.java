package boris.company.voicemovie.service;

import boris.company.voicemovie.exception.TaskNotFoundException;
import boris.company.voicemovie.model.MovieRequestTask;
import boris.company.voicemovie.model.TranscriptionResult;
import boris.company.voicemovie.model.dto.MovieSearchCriteria;
import boris.company.voicemovie.repository.MovieRequestTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieTaskStateService {

    private final MovieRequestTaskRepository repository;

    @Value("${scheduler.movie-request.retry.delays-seconds}")
    private List<Long> retryDelaysSeconds;

    @Transactional
    public void retryOrFail(
            Long taskId,
            String errorMessage,
            boolean retryable
    ) {
        MovieRequestTask task = findTask(taskId);

        int retryCount = task.getRetryCount();

        if (!retryable || retryCount >= retryDelaysSeconds.size()) {
            task.markFailed(errorMessage);
            return;
        }

        long delay = retryDelaysSeconds.get(retryCount);

        Instant nextRetryAt = Instant.now().plusSeconds(delay);

        task.scheduleRetry(errorMessage, nextRetryAt);
    }

    @Transactional
    public void startFirstStage(Long taskId) {
        MovieRequestTask task = findTask(taskId);

        switch (task.getInputType()) {
            case VOICE -> task.startTranscribing();
            case TEXT -> task.startExtractingCriteria();
        }

        resetRetryState(task);
    }

    @Transactional
    public void completeTranscription(Long taskId, TranscriptionResult result) {
        MovieRequestTask task = findTask(taskId);

        task.setProcessingStartedAt(Instant.now());

        task.saveTranscriptionResult(result);
        task.startExtractingCriteria();
        resetRetryState(task);
    }

    @Transactional
    public void completeCriteriaExtraction(Long taskId, MovieSearchCriteria criteria) {
        MovieRequestTask task = findTask(taskId);

        task.setProcessingStartedAt(Instant.now());

        task.setSearchCriteria(criteria);
        task.startSearchingMovies();
        resetRetryState(task);
    }

    private MovieRequestTask findTask(Long taskId) {
        return repository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private void resetRetryState(MovieRequestTask task) {
        task.setRetryCount(0);
        task.setNextRetryAt(null);
        task.setErrorMessage(null);
    }
}
