package boris.company.voicemovie.service;

import boris.company.voicemovie.model.dto.response.MovieDto;
import boris.company.voicemovie.model.dto.MovieSearchCriteria;

import java.util.List;

public interface MovieSearchService {
    List<MovieDto> search(MovieSearchCriteria criteria);}
