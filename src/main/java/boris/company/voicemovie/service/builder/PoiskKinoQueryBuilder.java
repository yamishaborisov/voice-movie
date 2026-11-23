package boris.company.voicemovie.service.builder;

import boris.company.voicemovie.model.dto.MovieSearchCriteria;
import boris.company.voicemovie.service.client.MovieSearchParam;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;

@Component
public class PoiskKinoQueryBuilder {

    private static final List<String> SELECT_FIELDS = List.of(
            "id", "name", "description", "premiere", "rating", "poster", "countries"
    );
    private static final List<String> NOT_NULL_FIELDS = List.of(
            "name", "premiere.world", "rating.kp", "poster.url", "countries.name"
    );

    public URI buildSearchUri(UriBuilder builder, MovieSearchCriteria criteria) {
        builder.path("/movie")
                .queryParam("selectFields", SELECT_FIELDS)
                .queryParam("notNullFields", NOT_NULL_FIELDS);

        addIfPresent(builder, MovieSearchParam.GENRE.getQueryParamName(), criteria.genre());
        addIfPresent(builder, MovieSearchParam.YEAR.getQueryParamName(), criteria.year());
        addIfPresent(builder, MovieSearchParam.RATING.getQueryParamName(), criteria.rating());

        builder.queryParam(MovieSearchParam.LIMIT.getQueryParamName(), 10);

        return builder.build();
    }

    private void addIfPresent(UriBuilder builder, String name, String value) {
        if (value != null && !value.isBlank()) {
            builder.queryParam(name, value);
        }
    }
}
