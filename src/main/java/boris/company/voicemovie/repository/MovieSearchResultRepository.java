package boris.company.voicemovie.repository;

import boris.company.voicemovie.model.MovieSearchResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieSearchResultRepository extends JpaRepository<MovieSearchResult, Long> {
    @EntityGraph(attributePaths = "movie")
    List<MovieSearchResult> findAllByMovieRequestTask_Id(Long taskId);
}
