package boris.company.voicemovie.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Table(name = "movies")
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tmdbId;

    private String movieTitle;

    private String description;

    private Double movieRating;

    private LocalDate releaseDate;

    private String posterPath;
}
