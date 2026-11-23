package boris.company.voicemovie.repository;

import boris.company.voicemovie.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByPoiskKinoId(Long poiskKinoId);
}
