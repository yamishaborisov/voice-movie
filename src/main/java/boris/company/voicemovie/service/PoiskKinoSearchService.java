package boris.company.voicemovie.service;

import boris.company.voicemovie.model.dto.response.MovieDto;
import boris.company.voicemovie.model.dto.MovieSearchCriteria;
import boris.company.voicemovie.model.dto.response.PoiskKinoSearchResponse;
import boris.company.voicemovie.service.client.PoiskKinoClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PoiskKinoSearchService implements MovieSearchService{

    private final PoiskKinoClient client;


    public PoiskKinoSearchService(
            PoiskKinoClient client
    ) {
        this.client = client;
    }

    @Override
    public List<MovieDto> search(MovieSearchCriteria criteria) {
        PoiskKinoSearchResponse response = client.search(criteria);

        if (response == null || response.docs() == null) {
            throw new IllegalStateException("ПоискКино вернул ответ без списка фильмов");
        }

        return response.docs().stream()
                .map(movie -> new MovieDto(
                        movie.id(),
                        movie.name(),
                        movie.description(),
                        movie.premiere().world().toLocalDate(),
                        movie.rating().kp(),
                        movie.poster().url(),
                        movie.countries().stream()
                                .map(PoiskKinoSearchResponse.Country::name)
                                .toList()
                ))
                .toList();
    }
}
