package boris.company.voicemovie.service;

import boris.company.voicemovie.exception.FileStorageException;
import boris.company.voicemovie.model.TranscriptionResult;
import boris.company.voicemovie.service.client.AssemblyAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class AssemblyAiService implements TranscriptionService{

    private final FileStorageService fileStorageService;
    private final AssemblyAiClient assemblyAiClient;

    @Override
    public TranscriptionResult transcribe(String fileReference) {
        Resource audio = readAudio(fileReference);
        return assemblyAiClient.transcribeAudio(audio);
    }

    private Resource readAudio(String fileReference) {
        try (InputStream stream = fileStorageService.getFile(fileReference)) {
            return new ByteArrayResource(stream.readAllBytes());
        } catch (IOException e) {
            throw new FileStorageException(
                    "Не удалось прочитать аудиофайл: " + fileReference,
                    e
            );
        }
    }
}
