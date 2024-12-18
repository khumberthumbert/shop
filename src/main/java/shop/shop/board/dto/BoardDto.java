package shop.shop.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import shop.shop.file.dto.FileMetadataDto;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime displayedTime;
    private List<FileMetadataDto> fileMetadataList;

    @Builder
    public BoardDto(int id, String title, String content, String user, LocalDateTime displayedTime, List<FileMetadataDto> fileMetadataList) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.user = user;
        this.displayedTime = displayedTime;
        this.fileMetadataList = fileMetadataList;

    }

    //private List<FileMetadataDto> fileMetadataList; // 첨부 파일 정보 리스트
}
