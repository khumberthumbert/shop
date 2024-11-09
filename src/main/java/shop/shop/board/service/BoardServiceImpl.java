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
import shop.shop.file.entity.FileMetadata;
import shop.shop.file.service.FileService;
import shop.shop.user.entity.UserEntity;
import shop.shop.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//https://github.com/Rookie8294/project-board-springboot-jpa/blob/master/src/main/java/com/project/board/dto/PostFormDto.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class BoardServiceImpl {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    //게시글 등록
    @Transactional
    public int saveBoard(int userId, BoardDto boardDto, List<MultipartFile> files) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));
        Board board = Board.builder()
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .writer(userEntity.getUsername())
                .user(userEntity)
                .build();


        // 첨부파일 처리
        if (files != null && !files.isEmpty()) {
            // FileService를 사용해 파일 저장
            List<FileMetadata> fileMetadataList = fileService.uploadFiles(files);

            // Board와 FileMetadata 관계 설정
            for (FileMetadata fileMetadata : fileMetadataList) {
                fileMetadata.setBoard(board);
            }
            board.getFileMetadataList().addAll(fileMetadataList); // 첨부파일 리스트 설정
        }

        boardRepository.save(board);
        return board.getId();
    }

    //게시글 수정
    @Transactional
    public int updateBoard(int boardId, BoardDto boardDto, List<MultipartFile> files) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 게시글 내용 업데이트
        board.update(boardDto.getTitle(), boardDto.getContent());

        // 기존 첨부파일 제거
        if (!board.getFileMetadataList().isEmpty()) {
            board.getFileMetadataList().clear();
        }

        // 새 첨부파일 처리
        if (files != null && !files.isEmpty()) {
            List<FileMetadata> fileMetadataList = fileService.uploadFiles(files);
            for (FileMetadata fileMetadata : fileMetadataList) {
                fileMetadata.setBoard(board);
            }
            board.getFileMetadataList().addAll(fileMetadataList);
        }

        return board.getId();
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(int boardId) {
        boardRepository.deleteById(boardId);
    }

    // 게시글 조회
    public BoardDto findBoardById(int boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return new BoardDto(board.getId(), board.getTitle(), board.getContent(), board.getUser().getUsername(), board.getDisplayedTime());
    }

    // 게시글 전체 조회
    public List<BoardDto> findAllPost(){
        //List<Post> list = postRepository.findAll();
        List<Board> list = boardRepository.findAllPostList();
        List<BoardDto> boardDtoList = new ArrayList<>();
        for( Board board : list ){
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
    public Page<BoardDto> findAllPostPage(Pageable pageable){

        //Page<Post> postPages = postRepository.findAll(PageRequest.of(pageable.getPageNumber() - 1, 3, Sort.by(Sort.Direction.DESC, "id")));
        Page<Board> boardPages = boardRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()));
        Page<BoardDto> boardResDto = boardPages.map(
                board -> BoardDto.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .user(board.getUser().getUsername())
                        .displayedTime(board.getDisplayedTime())  // 표시할 시간
                        .build()
        );

        List<Board> list = boardRepository.findAllPostList();
        list.forEach(board -> {
            System.out.println("Board ID: " + board.getId());
            System.out.println("Board Title: " + board.getTitle());
            System.out.println("Board Content: " + board.getContent());
            System.out.println("Board User: " + board.getUser().getUsername());
        });

        return boardResDto;
    }

    // 회원 게시글 조회(페이징)
    @PreAuthorize("isAuthenticated()")
    public Page<BoardDto> findAllPostPageById(Pageable pageable, int id){

        Page<Board> boardPages = boardRepository.findByUserId(PageRequest.of(pageable.getPageNumber() - 1, 3, Sort.by(Sort.Direction.DESC, "id")), id);

        Page<BoardDto> boardResDto = boardPages.map(
                board -> BoardDto.builder()
                        .id(board.getId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .user(board.getUser().getUsername())
                        .displayedTime(board.getDisplayedTime())
                        .build()
        );

        return boardResDto;
    }

}
