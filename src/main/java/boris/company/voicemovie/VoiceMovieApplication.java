package boris.company.voicemovie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VoiceMovieApplication{

    public static void main(String[] args) {
        SpringApplication.run(VoiceMovieApplication.class, args);
    }

}
