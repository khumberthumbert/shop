package shop.shop.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shop.shop.board.dto.BoardDto;
import shop.shop.board.service.BoardServiceImpl;
import shop.shop.user.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardRestController {

    private final BoardServiceImpl boardService;
    private final UserRepository userRepository;

    //게시글 등록
    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> saveBoard(@RequestPart("board") BoardDto boardDto,
                                             @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        // 현재 로그인된 사용자 ID 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 사용자 이름
        int userId = userRepository.findUserIdByUsername(username);

        // 게시글 저장
        int boardId = boardService.saveBoard(userId, boardDto, files);
        return ResponseEntity.ok(boardId);
    }

    // 게시글 수정
    @PostMapping("/update/{boardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> updateBoard(@PathVariable int boardId,
                                               @RequestPart("board") BoardDto boardDto,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        // 게시글 수정
        int updatedBoardId = boardService.updateBoard(boardId, boardDto, files);
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

    /*// 게시글 전체 조회
    @GetMapping("/all")
    public ResponseEntity<List<BoardDto>> findAllPost() {
        List<BoardDto> boardDtos = boardService.findAllPost();
        return ResponseEntity.ok(boardDtos);
    }*/

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
