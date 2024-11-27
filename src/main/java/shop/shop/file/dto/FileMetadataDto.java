package shop.shop.file.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDto {
    private Long id;            // 파일 ID
    private String fileName;   // 파일명
    private String fileType;   // 파일 타입
    private Long fileSize;     // 파일 크기
    private String fileUrl;     // HTTP URL
}

