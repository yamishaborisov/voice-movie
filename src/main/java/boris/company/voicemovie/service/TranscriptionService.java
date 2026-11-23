package boris.company.voicemovie.service;

import boris.company.voicemovie.model.TranscriptionResult;

public interface TranscriptionService {
    TranscriptionResult transcribe(String fileReference);
}
