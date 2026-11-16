package boris.company.voicemovie.client;

import boris.company.voicemovie.dto.MovieSearchCriteria;
import org.springframework.web.util.UriBuilder;

import java.util.function.Function;

public enum MovieSearchParam {
    GENRE("genres.name", c -> normalizeGenre(c.genre())),
    YEAR("year", c -> formatYearRange(c.yearFrom(), c.yearTo())),
    RATING("rating.kp", c -> formatMinRating(c.minRating())),
    LIMIT("limit", ignored -> String.valueOf(MovieSearchParam.DEFAULT_LIMIT));

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_YEAR_FROM = 1874;
    private static final int DEFAULT_YEAR_TO = 2099;
    private static final int MAX_RATING = 10;

    private final String apiName;
    private final Function<MovieSearchCriteria, String> formatter;

    MovieSearchParam(
            String apiName,
            Function<MovieSearchCriteria, String> formatter
    ) {
        this.apiName = apiName;
        this.formatter = formatter;
    }

    public void addQueryParam(UriBuilder builder, MovieSearchCriteria criteria) {
        String value = formatter.apply(criteria);

        if (value != null && !value.isBlank()) {
            builder.queryParam(apiName, value);
        }
    }

    private static String normalizeGenre(String genre) {
        return genre == null ? null : genre.strip();
    }

    private static String formatYearRange(Integer yearFrom, Integer yearTo) {
        if (yearFrom == null && yearTo == null) {
            return null;
        }

        int from = yearFrom != null ? yearFrom : DEFAULT_YEAR_FROM;
        int to = yearTo != null ? yearTo : DEFAULT_YEAR_TO;
        return from + "-" + to;
    }

    private static String formatMinRating(Double minRating) {
        return minRating == null ? null : minRating + "-" + MAX_RATING;
    }
}
