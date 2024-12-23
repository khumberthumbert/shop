///*
//package shop.shop.file.service;
//
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.io.FilenameUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import shop.shop.file.dto.FileMetadataDto;
//import shop.shop.file.entity.FileMetadata;
//import shop.shop.file.repository.FileMetadataRepository;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class FileServiceImpl implements FileService {
//
//    @Value("${file.upload-dir}")
//    private String uploadDir;
//
//    private final FileMetadataRepository fileMetadataRepository;
//
//    @Override
//    public String uploadFile(MultipartFile file) throws IOException {
//        String fileName = saveFileToServer(file);
//        saveFileMetadata(file, fileName);  // 파일 메타데이터 저장
//        return fileName;
//    }
//
//    @Override
//    public List<FileMetadataDto> uploadFiles(List<MultipartFile> files) {
//        List<FileMetadataDto> fileMetadataDtos = new ArrayList<>();
//        for (MultipartFile file : files) {
//            try {
//                // 파일 저장
//                String fileName = saveFileToServer(file);
//                FileMetadata metadata = saveFileMetadata(file, fileName); // 메타데이터 저장
//
//                // FileMetadataDto 생성 및 추가
//                FileMetadataDto fileMetadataDto = FileMetadataDto.builder()
//                        .fileName(metadata.getFileName())
//                        .fileType(metadata.getFileType())
//                        .fileSize(metadata.getFileSize())
//                        .fileUrl(metadata.getFilePath()) // 이미 저장된 HTTP URL 사용
//                        .build();
//
//                fileMetadataDtos.add(fileMetadataDto);
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
//            }
//        }
//        return fileMetadataDtos; // FileMetadataDto 리스트 반환
//    }
//
//    @Override
//    public List<FileMetadata> listAllFiles() {
//        return fileMetadataRepository.findAll();
//    }
//
//
//    @Override
//    public FileMetadata getFileMetadata(Long id) {
//        return fileMetadataRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));
//    }
//
//    @Override
//    public void deleteFile(Long id) {
//        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("File not found with ID: " + id));
//
//        // 서버 파일 삭제
//        String fullPath = uploadDir + File.separator + fileMetadata.getFileName();
//        deleteFileFromServer(fullPath);
//
//        // DB에서 메타데이터 삭제
//        fileMetadataRepository.delete(fileMetadata);
//    }
//
//
//    @Override
//    public FileMetadata getFileMetadataByFileName(String fileName) {
//        return fileMetadataRepository.findByFileName(fileName)
//                .orElseThrow(() -> new RuntimeException("File not found with name: " + fileName));
//    }
//
//    // 서버에 파일 저장
//    private String saveFileToServer(MultipartFile file) throws IOException {
//        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
//        String fileName = UUID.randomUUID().toString() + "." + extension; // 고유 파일명 생성
//
//        // 파일 저장 경로 (서버의 절대 경로)
//        Path filePath = Paths.get(uploadDir + File.separator + fileName);
//        Files.copy(file.getInputStream(), filePath);
//
//        // 반환 경로 (URL)
//        return fileName; // 절대 경로가 아닌 파일 이름만 반환
//    }
//
//
//    private FileMetadata saveFileMetadata(MultipartFile file, String fileName) {
//        FileMetadata metadata = new FileMetadata();
//        metadata.setFileName(fileName);
//        metadata.setFileType(file.getContentType());
//        metadata.setFileSize(file.getSize());
//        //metadata.setFilePath(uploadDir + File.separator + fileName); // 절대 경로 저장
//        // 절대 경로 대신 HTTP URL 경로 저장
//        metadata.setFilePath("/uploads/" + fileName);
//        fileMetadataRepository.save(metadata);
//        return metadata;
//    }
//
//
//    // 서버에서 파일 삭제 메서드 수정
//    public void deleteFileFromServer(String filePath) {
//        try {
//            // 파일 저장 경로로 변환
//            Path absolutePath = Paths.get(filePath);
//            File file = absolutePath.toFile();
//
//            if (file.exists() && !file.delete()) {
//                throw new RuntimeException("Failed to delete file: " + absolutePath);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Error while deleting file: " + filePath, e);
//        }
//    }
//
//}
//*/
