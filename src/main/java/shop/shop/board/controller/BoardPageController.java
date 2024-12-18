package shop.shop.board.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import shop.shop.board.dto.BoardDto;
import shop.shop.board.service.BoardServiceImpl;
import shop.shop.user.dto.CustomUserDetails;

@Controller
@RequiredArgsConstructor
@Log4j2
public class BoardPageController {

    private final BoardServiceImpl boardService;

    @GetMapping("/api/posts/page")
    public Object getBoardPage(@RequestHeader(value = "Accept", defaultValue = "text/html") String accept,
                               Model model, Pageable pageable)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Page<BoardDto> boardPage = boardService.findAllPostPage(pageable);



        if (accept.contains("application/json")) {
            // JSON 응답 반환
            return ResponseEntity.ok(boardPage);
        } else {
            log.info("user 확인용 {}", authentication.getName());
            model.addAttribute("username", authentication.getName());
            model.addAttribute("boardPage", boardPage);

            return "board/boardList :: boardListFragment";
        }
    }
}
