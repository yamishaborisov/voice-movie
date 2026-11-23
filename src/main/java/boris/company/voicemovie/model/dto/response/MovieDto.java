package boris.company.voicemovie.model.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MovieDto(
        Long poiskKinoId,
        String title,
        String description,
        LocalDate releaseDate,
        Double rating,
        String posterPath,
        List<String> countries
) {}
