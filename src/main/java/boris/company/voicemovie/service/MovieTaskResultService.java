package boris.company.voicemovie.service;

import boris.company.voicemovie.exception.TaskNotFoundException;
import boris.company.voicemovie.model.dto.converter.MovieConverter;
import boris.company.voicemovie.model.dto.converter.MovieSearchResultConverter;
import boris.company.voicemovie.model.dto.response.MovieDto;
import boris.company.voicemovie.model.Movie;
import boris.company.voicemovie.model.MovieRequestTask;
import boris.company.voicemovie.model.MovieSearchResult;
import boris.company.voicemovie.repository.MovieRepository;
import boris.company.voicemovie.repository.MovieRequestTaskRepository;
import boris.company.voicemovie.repository.MovieSearchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieTaskResultService {

    private final MovieRequestTaskRepository taskRepository;
    private final MovieRepository movieRepository;
    private final MovieSearchResultRepository resultRepository;

    @Transactional
    public void completeTask(Long taskId, List<MovieDto> movies) {
        MovieRequestTask task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Задача не найдена: " + taskId
                        )
                );
        for (MovieDto dto : movies) {
            Movie movie = movieRepository
                    .findByPoiskKinoId(dto.poiskKinoId())
                    .orElseGet(() -> movieRepository.save(
                            MovieConverter.toEntity(dto)
                    ));

            MovieSearchResult result =
                    MovieSearchResultConverter.toEntity(task, movie);

            resultRepository.save(result);
        }

        task.markCompleted();
    }

    @Transactional
    public void failTask(Long taskId, String failMessage) {
        MovieRequestTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        task.markFailed(failMessage);
    }
}
