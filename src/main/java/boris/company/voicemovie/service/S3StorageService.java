package boris.company.voicemovie.service;

import boris.company.voicemovie.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;

@Service
public class S3StorageService implements FileStorageService{

    private final S3Client s3Client;
    private final String bucket;


    public S3StorageService(S3Client s3Client,
                            @Value("${S3_BUCKET}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String saveFile(MultipartFile file){

        String fileKey = UUID.randomUUID().toString() + ".mp3";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType("audio/mpeg")
                .build();

        try (InputStream stream = file.getInputStream()) {
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(stream, file.getSize())
            );

            return fileKey;
        } catch (IOException e) {
            throw new FileStorageException("Не удалось прочитать файл", e);
        }
    }

    @Override
    public InputStream getFile(String fileKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        return s3Client.getObject(request);
    }
}
