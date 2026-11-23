package boris.company.voicemovie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
public class MovieSearchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private MovieRequestTask movieRequestTask;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;

}
