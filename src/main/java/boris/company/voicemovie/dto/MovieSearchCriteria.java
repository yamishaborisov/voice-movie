package boris.company.voicemovie.dto;

public record MovieSearchCriteria(
        String genre,
        Integer yearFrom,
        Integer yearTo,
        Double minRating
) {}

