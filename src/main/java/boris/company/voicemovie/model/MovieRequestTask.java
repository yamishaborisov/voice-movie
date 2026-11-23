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

    public void startExtractingCriteria() {
        this.status = MovieRequestTaskStatus.EXTRACTING_CRITERIA;
    }

    public void startSearchingMovies() {
        this.status = MovieRequestTaskStatus.SEARCHING_MOVIES;
    }

    public void markCompleted() {
        this.status = MovieRequestTaskStatus.COMPLETED;
    }

    public void markFailed(String errorMessage) {
        this.status = MovieRequestTaskStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void startTranscribing() {
        this.status = MovieRequestTaskStatus.TRANSCRIBING;
    }

    public void saveTranscriptionResult(TranscriptionResult transcriptionResult) {
        this.transcriptionResult = transcriptionResult;
        this.inputText = transcriptionResult.text();
    }
}
