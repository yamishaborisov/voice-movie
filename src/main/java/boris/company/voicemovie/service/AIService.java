package boris.company.voicemovie.service;

import boris.company.voicemovie.dto.MovieSearchCriteria;

public interface AIService {
    MovieSearchCriteria extractCriteria(String text);
}
