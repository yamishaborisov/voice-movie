package boris.company.voicemovie.model.dto.converter;

import boris.company.voicemovie.model.Movie;
import boris.company.voicemovie.model.MovieRequestTask;
import boris.company.voicemovie.model.MovieSearchResult;

public class MovieSearchResultConverter {
    private MovieSearchResultConverter() {}

    public static MovieSearchResult toEntity(
            MovieRequestTask task,
            Movie movie
    ) {
        MovieSearchResult result = new MovieSearchResult();
        result.setMovieRequestTask(task);
        result.setMovie(movie);
        return result;
    }
}
