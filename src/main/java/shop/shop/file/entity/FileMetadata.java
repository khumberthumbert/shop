package shop.shop.file.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import shop.shop.post.entity.Post;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post; // 파일이 속한 게시글 설정

}
