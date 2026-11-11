package boris.company.voicemovie.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {
    String saveFile(MultipartFile file);

    InputStream getFile(String fileKey);
}
