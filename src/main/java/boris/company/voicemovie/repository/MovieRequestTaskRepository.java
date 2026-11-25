package boris.company.voicemovie.repository;

import boris.company.voicemovie.model.MovieRequestTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface MovieRequestTaskRepository extends JpaRepository<MovieRequestTask, Long> {

    @Query(value = """
        SELECT *
        FROM movie_request_task
        WHERE status IN (
            'PENDING',
            'TRANSCRIBING',
            'EXTRACTING_CRITERIA',
            'SEARCHING_MOVIES'
        )
          AND (
              processing_started_at IS NULL
              OR processing_started_at < :expiredBefore
          )
          AND (
              next_retry_at IS NULL
              OR next_retry_at <= CURRENT_TIMESTAMP
          )
        ORDER BY created_at ASC, id ASC
        LIMIT :batchSize
        """, nativeQuery = true)
    List<MovieRequestTask> findAvailableTasks(
            @Param("batchSize") int batchSize,
            @Param("expiredBefore") Instant expiredBefore
    );

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE movie_request_task
        SET processing_started_at = :now,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :taskId
          AND (
                processing_started_at IS NULL
              OR processing_started_at < :expiredBefore
                  )
          AND status IN (
              'PENDING',
              'TRANSCRIBING',
              'EXTRACTING_CRITERIA',
              'SEARCHING_MOVIES'
          )
          AND (
              next_retry_at IS NULL
              OR next_retry_at <= CURRENT_TIMESTAMP
          )
        """, nativeQuery = true)
    int lockTask(
            @Param("taskId") Long taskId,
            @Param("now") Instant now,
            @Param("expiredBefore") Instant expiredBefore
    );

}
