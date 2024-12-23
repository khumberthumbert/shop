package shop.shop.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import shop.shop.file.dto.FileMetadataDto;
import shop.shop.file.entity.FileMetadata;
import shop.shop.file.repository.FileMetadataRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final S3Service s3Service; // S3Service 사용

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        // S3에 파일 업로드
        String fileName = s3Service.uploadFile(file);
        saveFileMetadata(file, fileName);  // 파일 메타데이터 저장
        return fileName;
    }

    @Override
    public List<FileMetadataDto> uploadFiles(List<MultipartFile> files) {
        List<FileMetadataDto> fileMetadataDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                // S3에 파일 저장
                String fileName = s3Service.uploadFile(file);
                FileMetadata metadata = saveFileMetadata(file, fileName); // 메타데이터 저장

                // FileMetadataDto 생성 및 추가
                FileMetadataDto fileMetadataDto = FileMetadataDto.builder()
                        .fileName(metadata.getFileName())
                        .fileType(metadata.getFileType())
                        .fileSize(metadata.getFileSize())
                        .fileUrl(metadata.getFilePath()) // 이미 저장된 HTTP URL 사용
                        .build();

                fileMetadataDtos.add(fileMetadataDto);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }
        return fileMetadataDtos; // FileMetadataDto 리스트 반환
    }

    @Override
    public List<FileMetadata> listAllFiles() {
        return fileMetadataRepository.findAll();
    }

    @Override
    public FileMetadata getFileMetadata(Long id) {
        return fileMetadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));
    }

    @Override
    public void deleteFile(Long id) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));

        // S3에서 파일 삭제
        s3Service.deleteFileFromS3(fileMetadata.getFileName());

        // DB에서 메타데이터 삭제
        fileMetadataRepository.delete(fileMetadata);
    }

    @Override
    public FileMetadata getFileMetadataByFileName(String fileName) {
        return fileMetadataRepository.findByFileName(fileName)
                .orElseThrow(() -> new RuntimeException("File not found with name: " + fileName));
    }

    // 파일 메타데이터 저장
    private FileMetadata saveFileMetadata(MultipartFile file, String fileName) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(fileName);
        metadata.setFileType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setFilePath(fileName); // 절대 경로 대신 HTTP URL 경로 저장
        fileMetadataRepository.save(metadata);
        return metadata;
    }
}
