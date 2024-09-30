package shop.shop.file.service;

import org.springframework.web.multipart.MultipartFile;
import shop.shop.file.entity.FileMetadata;

import java.io.IOException;
import java.util.List;

public interface FileService {
    String uploadFile(MultipartFile file) throws IOException;

    List<FileMetadata> listAllFiles();

    FileMetadata getFileMetadata(Long id);

    void deleteFile(Long id);
}
