package boris.company.voicemovie.model;

public record TranscriptionResult(String text,
                                  String llm_response,
                                  String llm_error,
                                  Float audio_duration_ms) {
}
