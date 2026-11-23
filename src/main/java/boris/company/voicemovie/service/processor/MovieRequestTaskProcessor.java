package boris.company.voicemovie.service.processor;

import boris.company.voicemovie.exception.TaskNotFoundException;
import boris.company.voicemovie.model.InputType;
import boris.company.voicemovie.model.MovieRequestTask;
import boris.company.voicemovie.model.MovieRequestTaskStatus;
import boris.company.voicemovie.model.TranscriptionResult;
import boris.company.voicemovie.repository.MovieRequestTaskRepository;
import boris.company.voicemovie.service.AIService;
import boris.company.voicemovie.service.MovieSearchService;
import boris.company.voicemovie.service.MovieTaskResultService;
import boris.company.voicemovie.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieRequestTaskProcessor {

    private final MovieRequestTaskRepository repository;
    private final AIService aiService;
    private final MovieSearchService movieSearchService;
    private final MovieTaskResultService resultService;
    private final TranscriptionService transcriptionService;


    public void process() {
        var tasks = repository.findByStatusOrderByCreatedAtAsc(
                MovieRequestTaskStatus.PENDING,
                PageRequest.of(0, 10)
        );

        for (var task : tasks) {
            try {
                processTask(task.getId());
            } catch (RuntimeException e) {
                log.error("Не удалось обработать задачу {}", task.getId(), e);
            }
        }
    }

    private void processTask(Long taskId) {
        MovieRequestTask task = repository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        try {
            if (task.getInputType() == InputType.VOICE) {
                task.startTranscribing();
                repository.save(task);

                TranscriptionResult result =
                        transcriptionService.transcribe(task.getFileReference());

                if (result == null || !StringUtils.hasText(result.text())) {
                    throw new IllegalStateException("Не удалось распознать речь");
                }
                task.saveTranscriptionResult(result);
            }

            task.startExtractingCriteria();
            repository.save(task);

            var criteria = aiService.extractCriteria(task.getInputText());
            task.setSearchCriteria(criteria);

            task.startSearchingMovies();
            repository.save(task);

            var movies = movieSearchService.search(criteria);

            resultService.completeTask(taskId, movies);
        } catch (RuntimeException e) {
            log.error("Ошибка обработки задачи {}", taskId, e);
            resultService.failTask(taskId, "Не удалось выполнить поиск фильмов");
        }
    }
}
