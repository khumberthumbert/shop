package shop.shop.board.dto;

import lombok.*;
import shop.shop.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class BoardDto {

    private int id;
    private String title;
    private String content;
    private String user;
    private LocalDateTime displayedTime;

    @Builder
    public BoardDto(int id, String title, String content, String user, LocalDateTime displayedTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.user = user;
        this.displayedTime = displayedTime;

    }

    //private List<FileMetadataDto> fileMetadataList; // 첨부 파일 정보 리스트
}
