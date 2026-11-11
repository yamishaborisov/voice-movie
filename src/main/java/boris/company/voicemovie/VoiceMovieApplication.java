package boris.company.voicemovie;

import boris.company.voicemovie.service.AIService;
import boris.company.voicemovie.service.S3StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VoiceMovieApplication{

//    private final AIService aiService;
    private final S3StorageService s3Service;

    public VoiceMovieApplication(S3StorageService s3Service) {
        this.s3Service = s3Service;
    }

    public static void main(String[] args) {
        SpringApplication.run(VoiceMovieApplication.class, args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println(s3Service.saveFile());
//    }
}
