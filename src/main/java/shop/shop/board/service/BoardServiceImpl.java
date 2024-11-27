package shop.shop.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shop.shop.board.dto.BoardDto;
import shop.shop.board.entity.Board;
import shop.shop.board.repository.BoardRepository;
import shop.shop.file.dto.FileMetadataDto;
import shop.shop.file.entity.FileMetadata;
import shop.shop.file.service.FileService;
import shop.shop.user.entity.UserEntity;
import shop.shop.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class BoardServiceImpl {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    // 게시글 등록
    @Transactional
    public int saveBoard(int userId, BoardDto boardDto, List<MultipartFile> files) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

        Board board = Board.builder()
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .writer(userEntity.getUsername())
                .user(userEntity)
                .build();

        // 첨부파일 처리
        if (files != null && !files.isEmpty()) {
            List<FileMetadataDto> fileMetadataDtos = fileService.uploadFiles(files);

            List<FileMetadata> fileMetadataList = fileMetadataDtos.stream()
                    .map(dto -> {
                        FileMetadata metadata = new FileMetadata();
                        metadata.setFileName(dto.getFileName());
                        metadata.setFileType(dto.getFileType());
                        metadata.setFileSize(dto.getFileSize());
                        metadata.setFilePath(dto.getFileUrl());
                        return metadata;
                    }).toList();

            for (FileMetadata fileMetadata : fileMetadataList) {
                fileMetadata.setBoard(board);
            }

            board.getFileMetadataList().addAll(fileMetadataList);
        }

        boardRepository.save(board);
        return board.getId();
    }

    // 게시글 수정
    @Transactional
    public int updateBoard(int boardId, BoardDto boardDto, List<MultipartFile> files, List<Long> deleteFileIds) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 게시글 내용 업데이트
        board.update(boardDto.getTitle(), boardDto.getContent());

        // 기존 첨부파일 삭제 처리
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            deleteFileIds.forEach(fileId -> {
                FileMetadata fileMetadata = fileService.getFileMetadata(fileId);
                if (fileMetadata != null) {
                    // 실제 파일 삭제
                    fileService.deleteFile(fileId);
                    log.info("파일 삭제 완료: {}", fileMetadata.getFileName());
                    // Board 엔티티의 첨부파일 리스트에서 제거
                    board.getFileMetadataList().remove(fileMetadata);
                }
            });
        }

        // 새 첨부파일 추가 처리
        if (files != null && !files.isEmpty()) {
            List<FileMetadataDto> fileMetadataDtos = fileService.uploadFiles(files);

            List<FileMetadata> fileMetadataList = fileMetadataDtos.stream()
                    .map(dto -> {
                        FileMetadata metadata = new FileMetadata();
                        metadata.setFileName(dto.getFileName());
                        metadata.setFileType(dto.getFileType());
                        metadata.setFileSize(dto.getFileSize());
                        metadata.setFilePath(dto.getFileUrl());
                        return metadata;
                    }).toList();

            for (FileMetadata fileMetadata : fileMetadataList) {
                fileMetadata.setBoard(board);
            }

            board.getFileMetadataList().addAll(fileMetadataList);
        }
        log.info("게시글 수정 완료. 게시글 ID: {}", boardId);
        return board.getId();
    }



    // 게시글 삭제
    @Transactional
    public void deleteBoard(int boardId) {
        boardRepository.deleteById(boardId);
    }

    // 게시글 단건 조회
    public BoardDto findBoardById(int boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<FileMetadataDto> fileMetadataDtos = board.getFileMetadataList().stream()
                .map(file -> {
                    System.out.println("Original filePath from DB: " + file.getFilePath());
                    return FileMetadataDto.builder()
                            .id(file.getId())
                            .fileName(file.getFileName())
                            .fileType(file.getFileType())
                            .fileSize(file.getFileSize())
                            .fileUrl(file.getFilePath()) // HTTP URL 그대로 사용
                            .build();
                })
                .toList();

        return BoardDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .user(board.getUser().getUsername())
                .displayedTime(board.getDisplayedTime())
                .fileMetadataList(fileMetadataDtos)
                .build();
    }

    // 게시글 전체 조회
    public List<BoardDto> findAllPost() {
        List<Board> list = boardRepository.findAllPostList();
        List<BoardDto> boardDtoList = new ArrayList<>();
        for (Board board : list) {
            BoardDto boardDto = BoardDto.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .user(board.getUser().getUsername())
                    .displayedTime(board.getDisplayedTime())
                    .build();
            log.info("{} 보드디티오 정보보기 ", boardDto);
            boardDtoList.add(boardDto);
        }
        return boardDtoList;
    }

    // 게시글 전체 조회(페이징)
    public Page<BoardDto> findAllPostPage(Pageable pageable) {
        Page<Board> boardPages = boardRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()));
        return boardPages.map(
                board -> BoardDto.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .user(board.getUser().getUsername())
                        .displayedTime(board.getDisplayedTime())
                        .build()
        );
    }

    // 회원 게시글 조회(페이징)
    @PreAuthorize("isAuthenticated()")
    public Page<BoardDto> findAllPostPageById(Pageable pageable, int id) {
        Page<Board> boardPages = boardRepository.findByUserId(PageRequest.of(pageable.getPageNumber() - 1, 3, Sort.by(Sort.Direction.DESC, "id")), id);
        return boardPages.map(
                board -> BoardDto.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .user(board.getUser().getUsername())
                        .displayedTime(board.getDisplayedTime())
                        .build()
        );
    }
}
