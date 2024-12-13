package shop.shop.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import shop.shop.board.dto.BoardDto;
import shop.shop.board.service.BoardServiceImpl;

@Controller
@RequiredArgsConstructor
public class BoardPageController {

    private final BoardServiceImpl boardService;

    @GetMapping("/api/posts/page")
    public Object getBoardPage(@RequestHeader(value = "Accept", defaultValue = "text/html") String accept, Model model, Pageable pageable) {
        Page<BoardDto> boardPage = boardService.findAllPostPage(pageable);

        // 디버깅: 전달받은 데이터 확인
        boardPage.getContent().forEach(board -> {
            System.out.println("BoardDto ID: " + board.getId());
            System.out.println("BoardDto Title: " + board.getTitle());
        });

        if (accept.contains("application/json")) {
            // JSON 응답 반환
            return ResponseEntity.ok(boardPage);
        } else {
            // HTML 프래그먼트 반환
            model.addAttribute("boardPage", boardPage);
            return "board/boardList :: boardListFragment";
        }
    }
}
