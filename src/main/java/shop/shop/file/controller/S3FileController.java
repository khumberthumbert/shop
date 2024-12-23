package shop.shop.file.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shop.shop.file.dto.FileMetadataDto;
import shop.shop.file.entity.FileMetadata;
import shop.shop.file.service.FileService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/s3/files")
@RequiredArgsConstructor
public class S3FileController {

    private final FileService fileService;  // FileServiceImpl에 대한 의존성 주입

    // S3에 파일 업로드 요청 처리
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // S3에 파일 업로드 및 메타데이터 저장
            String fileName = fileService.uploadFile(file);
            return new ResponseEntity<>("File uploaded to S3: " + fileName, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload file to S3", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 여러 파일 S3에 업로드 요청 처리
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<FileMetadataDto>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files) {
        try {
            // 여러 파일을 S3에 업로드하고 메타데이터 반환
            List<FileMetadataDto> fileMetadataDtos = fileService.uploadFiles(files);
            return new ResponseEntity<>(fileMetadataDtos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 모든 파일 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<List<FileMetadata>> listAllFiles() {
        List<FileMetadata> files = fileService.listAllFiles();
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    // 특정 파일 메타데이터 조회
    @GetMapping("/file/{id}")
    public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable Long id) {
        FileMetadata fileMetadata = fileService.getFileMetadata(id);
        return new ResponseEntity<>(fileMetadata, HttpStatus.OK);
    }

    // 파일 삭제 요청 처리 (S3 환경)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return new ResponseEntity<>("File deleted from S3", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
