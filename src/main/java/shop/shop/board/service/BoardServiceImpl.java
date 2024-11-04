package shop.shop.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.shop.board.dto.BoardDto;
import shop.shop.board.entity.Board;
import shop.shop.board.repository.BoardRepository;
import shop.shop.user.entity.UserEntity;
import shop.shop.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
//https://github.com/Rookie8294/project-board-springboot-jpa/blob/master/src/main/java/com/project/board/dto/PostFormDto.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardServiceImpl {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    //게시글 등록
    @Transactional
    public int saveBoard(int userId, BoardDto boardDto) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));
        Board board = Board.builder()
                .title(boardDto.getTitle())
                .content(boardDto.getContent())
                .writer(userEntity.getUsername())
                .user(userEntity)
                .build();

        boardRepository.save(board);
        return board.getId();
    }

    // 게시글 수정
    @Transactional
    public int updateBoard(int boardId, BoardDto boardDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        board.update(boardDto.getTitle(), boardDto.getContent());
        return boardId;
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
