package shop.shop.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import shop.shop.user.config.JwtUtil;
import shop.shop.user.dto.AuthenticationRequest;
import shop.shop.user.service.CustomUserDetailsService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/api/auth/login")
    @ResponseBody
    public Map<String, String> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String accessToken = jwtUtil.generateToken(userDetails.getUsername());
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername()); // Refresh Token 생성

        // Access Token과 Refresh Token을 JSON 형태로 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken); // 클라이언트에 Refresh Token 제공

        return response;
    }

    // Refresh Token을 통한 Access Token 재발급
    @PostMapping("/api/auth/refresh")
    @ResponseBody
    public Map<String, String> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String username = jwtUtil.extractUsername(refreshToken); // Refresh Token에서 사용자명 추출

        // Refresh Token 검증
        if (jwtUtil.validateToken(refreshToken, userDetailsService.loadUserByUsername(username))) {
            String newAccessToken = jwtUtil.generateToken(username); // 새로운 Access Token 발급
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return response;
        } else {
            throw new RuntimeException("Invalid Refresh Token");
        }
    }

    @GetMapping("/api/auth/check")
    @ResponseBody
    public ResponseEntity<String> checkLoginStatus() {
        // SecurityContextHolder에서 인증된 사용자를 확인
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return ResponseEntity.ok("User is logged in");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not logged in");
        }
    }


    @GetMapping("/loginPage")
    public String loginPage() {
        return "user/loginPage";
    }
}

