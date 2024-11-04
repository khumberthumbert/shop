package shop.shop.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import shop.shop.board.dto.BoardDto;
import shop.shop.board.service.BoardServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardServiceImpl boardService;

    // 게시글 등록
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/save")
    public ResponseEntity<Integer> saveBoard(@RequestParam int userId, @RequestBody BoardDto boardDto) {
        int boardId = boardService.saveBoard(userId, boardDto);
        return ResponseEntity.ok(boardId);
    }

    // 게시글 수정
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{boardId}")
    public ResponseEntity<Integer> updateBoard(@PathVariable int boardId, @RequestBody BoardDto boardDto) {
        int updatedBoardId = boardService.updateBoard(boardId, boardDto);
        return ResponseEntity.ok(updatedBoardId);
    }

    // 게시글 삭제
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok().build();
    }

    // 게시글 단건 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto> findBoardById(@PathVariable int boardId) {
        BoardDto boardDto = boardService.findBoardById(boardId);
        return ResponseEntity.ok(boardDto);
    }

    // 게시글 전체 조회
    @GetMapping("/all")
    public ResponseEntity<List<BoardDto>> findAllPost() {
        List<BoardDto> boardDtos = boardService.findAllPost();
        return ResponseEntity.ok(boardDtos);
    }

    // 게시글 전체 조회(페이징)
    @GetMapping("/page")
    public ResponseEntity<Page<BoardDto>> findAllPostPage(Pageable pageable) {
        Page<BoardDto> boardDtos = boardService.findAllPostPage(pageable);
        return ResponseEntity.ok(boardDtos);
    }

    // 회원 게시글 조회(페이징)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}/page")
    public ResponseEntity<Page<BoardDto>> findAllPostPageByUserId(Pageable pageable, @PathVariable int userId) {
        Page<BoardDto> boardDtos = boardService.findAllPostPageById(pageable, userId);
        return ResponseEntity.ok(boardDtos);
    }
}
