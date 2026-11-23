package boris.company.voicemovie.model.dto.converter;

import boris.company.voicemovie.model.Movie;
import boris.company.voicemovie.model.dto.response.MovieDto;

public class MovieConverter {

    private MovieConverter() {}

    public static MovieDto toDto(Movie movie) {
        return new MovieDto(
                movie.getPoiskKinoId(),
                movie.getMovieTitle(),
                movie.getDescription(),
                movie.getReleaseDate(),
                movie.getMovieRating(),
                movie.getPosterPath(),
                movie.getCountries()
        );
    }

    public static Movie toEntity(MovieDto dto) {
        Movie movie = new Movie();
        movie.setPoiskKinoId(dto.poiskKinoId());
        movie.setMovieTitle(dto.title());
        movie.setDescription(dto.description());
        movie.setMovieRating(dto.rating());
        movie.setReleaseDate(dto.releaseDate());
        movie.setCountries(dto.countries());
        movie.setPosterPath(dto.posterPath());
        return movie;
    }
}
