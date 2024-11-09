package shop.shop.board.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class BoardController {

    @GetMapping("/board/writeFragment")
    public String getWriteFragment() {
        return "board/boardWriteFragment :: boardWriteFragment";
    }
}
