package shop.shop.file.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import shop.shop.file.entity.FileMetadata;
import shop.shop.file.repository.FileMetadataRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileMetadataRepository fileMetadataRepository;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = saveFileToServer(file);
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(fileName);
        metadata.setFileType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setFilePath(uploadDir + File.separator + fileName);
        fileMetadataRepository.save(metadata);  // JPA를 통해 메타데이터 저장
        return fileName;
    }

    @Override
    public List<FileMetadata> listAllFiles() {
        return fileMetadataRepository.findAll();
    }

    @Override
    public FileMetadata getFileMetadata(Long id) {
        return fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }

    @Override
    public void deleteFile(Long id) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
        File file = new File(fileMetadata.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        fileMetadataRepository.delete(fileMetadata);
    }

    private String saveFileToServer(MultipartFile file) throws IOException {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "." + extension;  // 유니크 파일 이름 생성
        Path filePath = Paths.get(uploadDir + File.separator + fileName);
        Files.copy(file.getInputStream(), filePath);
        return fileName;
    }
}

