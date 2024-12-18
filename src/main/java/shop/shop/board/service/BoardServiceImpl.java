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
            for (Long fileId : deleteFileIds) {
                try {
                    FileMetadata fileMetadata = fileService.getFileMetadata(fileId);
                    String filePath = fileMetadata.getFilePath();

                    log.info("Attempting to delete file: {}", filePath); // 삭제 시도 로그
                    board.getFileMetadataList().remove(fileMetadata); // Board의 파일 목록에서 제거
                    fileService.deleteFile(fileId); // 실제 파일 삭제

                    log.info("Successfully deleted file: {}", filePath); // 삭제 성공 로그
                } catch (RuntimeException e) {
                    log.error("Failed to delete file with ID: {}", fileId, e); // 삭제 실패 로그
                }
            }
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
                        metadata.setFilePath(dto.getFileUrl()); // HTTP URL 저장
                        return metadata;
                    }).toList();

            for (FileMetadata fileMetadata : fileMetadataList) {
                fileMetadata.setBoard(board);
            }

            board.getFileMetadataList().addAll(fileMetadataList);
        }

        return board.getId();
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(int boardId, String username) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!board.getWriter().equals(username)) {
            throw new SecurityException("게시글 삭제 권한이 없습니다.");
        }

        // 첨부파일 삭제
        if (board.getFileMetadataList() != null && !board.getFileMetadataList().isEmpty()) {
            for (FileMetadata fileMetadata : board.getFileMetadataList()) {
                fileService.deleteFile(fileMetadata.getId()); // 실제 파일 및 메타데이터 삭제
            }
        }

        // 게시글 삭제
        boardRepository.delete(board);
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

    // 게시글 전체 조회(페이징)
    public Page<BoardDto> findAllPostPage(Pageable pageable) {
        // Sort 객체를 생성하여 id 기준 내림차순 정렬
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("id")) // id 기준 내림차순 정렬
        );

        // 정렬된 pageable을 사용하여 게시글 조회
        Page<Board> boardPages = boardRepository.findAll(sortedPageable);

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
