package boris.company.voicemovie.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class MovieSearchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private MovieRequestTask movieRequestTask;

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;


}
