package boris.company.voicemovie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Getter
@Table(name = "movies")
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private Long poiskKinoId;

    @Setter
    private String movieTitle;

    @Setter
    private String description;

    @Setter
    private Double movieRating;

    @Setter
    private LocalDate releaseDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Setter
    private List<String> countries;

    @Setter
    private String posterPath;
}
