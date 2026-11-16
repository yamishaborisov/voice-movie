package boris.company.voicemovie.service;

import boris.company.voicemovie.dto.response.MovieDto;
import boris.company.voicemovie.dto.MovieSearchCriteria;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MovieSearchService {
    Mono<List<MovieDto>> search(MovieSearchCriteria criteria);}
