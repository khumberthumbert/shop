package shop.shop.file.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shop.shop.file.entity.FileMetadata;
import shop.shop.file.service.FileService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // 파일 업로드 요청 처리
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = fileService.uploadFile(file);
            return new ResponseEntity<>("File uploaded: " + fileName, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR);
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

    // 파일 삭제 요청 처리
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return new ResponseEntity<>("File deleted", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}

