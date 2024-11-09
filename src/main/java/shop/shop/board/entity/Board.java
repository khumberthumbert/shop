package shop.shop.board.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.shop.file.entity.FileMetadata;
import shop.shop.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Board{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileMetadata> fileMetadataList = new ArrayList<>(); // 여러 개의 첨부파일을 가질 수 있도록 설정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 외래 키 설정
    private UserEntity user; // 작성자 정보


    @Builder
    public Board(int id, String title, String content, String writer, LocalDateTime createdAt, LocalDateTime updatedAt, List<FileMetadata> fileMetadataList, UserEntity user) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.fileMetadataList = fileMetadataList != null ? fileMetadataList : new ArrayList<>();
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 동적 시간 반환 메서드
    public LocalDateTime getDisplayedTime() {
        return updatedAt.isAfter(createdAt) ? updatedAt : createdAt;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

}

