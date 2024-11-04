package shop.shop;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.Iterator;

@Controller
@Log4j2
public class HomeController {

    @GetMapping("/")
    public String home(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("사용자 이름 : {}", name);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();
        String role = auth.getAuthority();

        log.info("사용자 role : {}", role);

        return "mainpage/main";
    }
}
