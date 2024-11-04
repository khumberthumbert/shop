package shop.shop.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import shop.shop.board.dto.BoardDto;
import shop.shop.board.service.BoardServiceImpl;

@Controller
@RequiredArgsConstructor
public class BoardPageController {

    private final BoardServiceImpl boardService;

    @GetMapping("/api/posts/page")
    public String getBoardFragment(Model model, Pageable pageable) {
        Page<BoardDto> boardPage = boardService.findAllPostPage(pageable);
        model.addAttribute("boardPage", boardPage);
        return "board/boardList :: boardListFragment";  // boardListFragment 프래그먼트만 반환
    }
}