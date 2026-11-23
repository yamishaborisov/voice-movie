package boris.company.voicemovie.model.dto;

import boris.company.voicemovie.model.MovieRequestTaskStatus;
import boris.company.voicemovie.model.dto.response.MovieDto;

import java.util.List;

public record MovieSearchResultResponse(
        Long taskId,
        MovieRequestTaskStatus status,
        List<MovieDto> movies,
        String errorMessage
) {
}
