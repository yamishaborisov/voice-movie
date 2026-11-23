package boris.company.voicemovie.command;

import org.springframework.web.multipart.MultipartFile;

public record VoiceSearchCommand(MultipartFile file) implements MovieSearchCommand {
}
