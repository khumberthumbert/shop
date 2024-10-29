package shop.shop.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @GetMapping("/loginPage")
    public ResponseEntity<Map<String, String>> getLoginPageData() {
        Map<String, String> data = new HashMap<>();
        data.put("formAction","/login"); //로그인 폼 제출 uRL
        data.put("usernamePlaceholder", "username");
        data.put("passwordPlaceholder", "password");
        data.put("loginButtonText", "login");

        return ResponseEntity.ok(data);
    }
}
