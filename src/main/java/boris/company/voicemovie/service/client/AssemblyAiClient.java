package boris.company.voicemovie.service.client;

import boris.company.voicemovie.model.TranscriptionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class AssemblyAiClient {

    private final RestClient restClient;

    public AssemblyAiClient(
            RestClient.Builder builder,
            @Value("${assembly.api-key}") String apiKey,
            @Value("${assembly.url}") String url
    ){
        this.restClient = builder
                .baseUrl(url)
                .defaultHeader("Authorization", apiKey)
                .build();
    }

    public TranscriptionResult transcribeAudio(Resource audio)
    {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("audio", audio);

        return restClient.post()
                .uri("/transcribe")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(builder.build())
                .retrieve()
                .body(TranscriptionResult.class);
    }

}
