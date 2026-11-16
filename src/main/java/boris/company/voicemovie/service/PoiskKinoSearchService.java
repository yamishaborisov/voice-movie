package boris.company.voicemovie.service;

import boris.company.voicemovie.client.MovieSearchParam;
import boris.company.voicemovie.dto.response.MovieDto;
import boris.company.voicemovie.dto.MovieSearchCriteria;
import boris.company.voicemovie.dto.response.PoiskKinoSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Service
public class PoiskKinoSearchService implements MovieSearchService{

    private static final Duration SEARCH_TIMEOUT = Duration.ofSeconds(10);
    private static final List<String> SELECT_FIELDS = List.of(
            "id", "name", "description", "premiere", "rating", "poster", "countries"
    );

    private final WebClient webClient;

    public PoiskKinoSearchService(
            WebClient.Builder builder,
            @Value("${poisk-kino.token}") String apiKey,
            @Value("${poisk-kino.base-url}") String url
    ) {
        this.webClient = builder
                .baseUrl(url)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
    }

    @Override
    public Mono<List<MovieDto>> search(MovieSearchCriteria criteria) {
        return webClient.get()
                .uri(builder -> buildSearchUri(builder, criteria))
                .retrieve()
                .bodyToMono(PoiskKinoSearchResponse.class)
                .map(response -> response.docs().stream()
                        .map(this::toMovieDto)
                        .toList())
                .timeout(SEARCH_TIMEOUT);
    }

    private MovieDto toMovieDto(PoiskKinoSearchResponse.MovieItem movie) {
        return new MovieDto(
                movie.id(),
                movie.name(),
                movie.description(),
                movie.premiere() == null || movie.premiere().world() == null
                        ? null : movie.premiere().world().toLocalDate(),
                movie.rating() == null ? null : movie.rating().kp(),
                movie.poster() == null ? null : movie.poster().url(),
                movie.countries() == null ? List.of() : movie.countries().stream()
                        .map(PoiskKinoSearchResponse.Country::name)
                        .toList()
        );
    }


    private URI buildSearchUri(UriBuilder builder, MovieSearchCriteria criteria) {
        builder.path("/movie")
                .queryParam("selectFields", SELECT_FIELDS);

        for (MovieSearchParam param : MovieSearchParam.values()) {
            param.addQueryParam(builder, criteria);
        }

        return builder.build();
    }
}
