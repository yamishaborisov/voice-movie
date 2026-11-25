package boris.company.voicemovie.model;

import boris.company.voicemovie.model.dto.MovieSearchCriteria;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
public class MovieRequestTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Setter
    private MovieRequestTaskStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Setter
    private InputType inputType;

    @Setter
    private String fileReference;

    @Setter
    private String inputText;

    @Setter
    private String errorMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    private TranscriptionResult transcriptionResult;

    @Setter
    @JdbcTypeCode(SqlTypes.JSON)
    private MovieSearchCriteria searchCriteria;

    @Setter
    private int retryCount = 0;

    @Setter
    private Instant processingStartedAt;

    @Setter
    private Instant nextRetryAt;

    public void startExtractingCriteria() {
        this.status = MovieRequestTaskStatus.EXTRACTING_CRITERIA;
    }

    public void startSearchingMovies() {
        this.status = MovieRequestTaskStatus.SEARCHING_MOVIES;
    }

    public void markCompleted() {
        this.status = MovieRequestTaskStatus.COMPLETED;
        this.processingStartedAt = null;
        this.retryCount = 0;
        this.nextRetryAt = null;
        this.errorMessage = null;    }

    public void markFailed(String errorMessage) {
        this.status = MovieRequestTaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processingStartedAt = null;
        this.nextRetryAt = null;
    }

    public void scheduleRetry(String errorMessage, Instant nextRetryAt) {
        this.retryCount++;
        this.nextRetryAt = nextRetryAt;
        this.errorMessage = errorMessage;
        this.processingStartedAt = null;
    }

    public void startTranscribing() {
        this.status = MovieRequestTaskStatus.TRANSCRIBING;
    }

    public void saveTranscriptionResult(TranscriptionResult transcriptionResult) {
        this.transcriptionResult = transcriptionResult;
        this.inputText = transcriptionResult.text();
    }
}
