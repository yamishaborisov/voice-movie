package boris.company.voicemovie.model;

public enum MovieRequestTaskStatus {
    PENDING,
    TRANSCRIBING,
    EXTRACTING_CRITERIA,
    SEARCHING_MOVIES,
    COMPLETED,
    FAILED
}
