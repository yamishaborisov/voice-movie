package boris.company.voicemovie.service;

import boris.company.voicemovie.model.dto.MovieSearchCriteria;

public interface AIService {
    MovieSearchCriteria extractCriteria(String text);
}
