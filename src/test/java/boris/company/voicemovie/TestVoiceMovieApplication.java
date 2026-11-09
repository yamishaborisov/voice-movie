package boris.company.voicemovie;

import org.springframework.boot.SpringApplication;

public class TestVoiceMovieApplication {

    public static void main(String[] args) {
        SpringApplication.from(VoiceMovieApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
