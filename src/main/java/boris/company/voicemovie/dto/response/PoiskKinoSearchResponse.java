package boris.company.voicemovie.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PoiskKinoSearchResponse(
        List<MovieItem> docs
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MovieItem(
            Long id,
            String name,
            String description,
            Premiere premiere,
            Rating rating,
            Poster poster,
            List<Country> countries
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Rating(Double kp) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Poster(String url) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Country(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Premiere(OffsetDateTime world) {}
}
