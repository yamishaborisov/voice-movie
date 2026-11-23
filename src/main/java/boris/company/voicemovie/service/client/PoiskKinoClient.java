package boris.company.voicemovie.service.client;

import boris.company.voicemovie.model.dto.MovieSearchCriteria;
import boris.company.voicemovie.model.dto.response.PoiskKinoSearchResponse;
import boris.company.voicemovie.service.builder.PoiskKinoQueryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PoiskKinoClient {

    private final RestClient restClient;
    private final PoiskKinoQueryBuilder queryBuilder;

    public PoiskKinoClient(
            RestClient.Builder builder,
            @Value("${poisk-kino.token}") String apiKey,
            @Value("${poisk-kino.base-url}") String url,
            PoiskKinoQueryBuilder queryBuilder
    ) {
        this.restClient = builder
                .baseUrl(url)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
        this.queryBuilder = queryBuilder;
    }

    public PoiskKinoSearchResponse search(MovieSearchCriteria criteria) {
        return restClient.get()
                .uri(builder -> queryBuilder.buildSearchUri(builder, criteria))
                .retrieve()
                .body(PoiskKinoSearchResponse.class);
    }

}
