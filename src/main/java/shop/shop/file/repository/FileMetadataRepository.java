package shop.shop.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.shop.file.entity.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}
