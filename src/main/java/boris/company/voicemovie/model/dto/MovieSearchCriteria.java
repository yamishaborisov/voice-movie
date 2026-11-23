package boris.company.voicemovie.model.dto;

public record MovieSearchCriteria(
        String genre,
        String year,
        String rating
) {}

