package boris.company.voicemovie.service;

import boris.company.voicemovie.command.MovieSearchCommand;
import boris.company.voicemovie.command.TextSearchCommand;
import boris.company.voicemovie.command.VoiceSearchCommand;
import boris.company.voicemovie.exception.MovieTaskSaveException;
import boris.company.voicemovie.exception.TaskNotFoundException;
import boris.company.voicemovie.model.MovieRequestTask;
import boris.company.voicemovie.model.MovieRequestTaskStatus;
import boris.company.voicemovie.model.dto.MovieSearchResultResponse;
import boris.company.voicemovie.model.dto.converter.MovieConverter;
import boris.company.voicemovie.model.dto.converter.MovieRequestTaskConverter;
import boris.company.voicemovie.model.dto.response.MovieDto;
import boris.company.voicemovie.repository.MovieRequestTaskRepository;
import boris.company.voicemovie.repository.MovieSearchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieTaskService {

    private final MovieRequestTaskRepository movieRequestTaskRepository;
    private final FileStorageService fileStorageService;
    private final MovieSearchResultRepository movieSearchResultRepository;


    public MovieRequestTask startMovieSearch(MovieSearchCommand command) {
        return switch (command) {
            case TextSearchCommand textCommand -> {
                MovieRequestTask task =
                        MovieRequestTaskConverter.toEntity(textCommand.text());

                yield movieRequestTaskRepository.save(task);
            }

            case VoiceSearchCommand voiceCommand -> {
                String fileReference = fileStorageService.saveFile(voiceCommand.file());

                try {
                    MovieRequestTask task =
                            MovieRequestTaskConverter.toVoiceEntity(fileReference);

                    yield movieRequestTaskRepository.save(task);
                } catch (RuntimeException saveException) {
                    try {
                        fileStorageService.deleteFile(fileReference);
                    } catch (RuntimeException deleteException) {
                        log.error(
                                "Не удалось удалить файл из S3: {}",
                                fileReference,
                                deleteException
                        );
                    }

                    throw new MovieTaskSaveException(saveException);
                }

            }

            default -> throw new IllegalArgumentException(
                    "Неизвестный тип команды: " + command.getClass().getName());
        };
    }

    @Transactional(readOnly = true)
    public MovieSearchResultResponse getSearchResult(Long taskId) {
        MovieRequestTask task = movieRequestTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        List<MovieDto> movies = List.of();

        if (task.getStatus() == MovieRequestTaskStatus.COMPLETED) {
            movies = movieSearchResultRepository
                    .findAllByMovieRequestTask_Id(taskId)
                    .stream()
                    .map(result -> MovieConverter.toDto(result.getMovie()))
                    .toList();
        }

        return new MovieSearchResultResponse(
                task.getId(),
                task.getStatus(),
                movies,
                task.getErrorMessage()
        );
    }
}
