package shop.shop.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.shop.file.entity.FileMetadata;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByFileName(String fileName); // 파일 이름으로 파일 메타데이터 조회
}
