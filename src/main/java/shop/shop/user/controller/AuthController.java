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

    /**
     * 로그인 인증 처리.
     *
     * @param authenticationRequest
     * @return
     * @throws Exception
     */
    @PostMapping("/api/auth/login")
    @ResponseBody
    public Map<String, String> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        // 사용자 인증
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );

        //사용자 정보 로드
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        // Access Token 및 Refresh Token 생성
        final String accessToken = jwtUtil.generateToken(userDetails.getUsername());
        final String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

        // Access Token과 Refresh Token을 JSON 형태로 반환
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return response;
    }

    // Refresh Token을 통한 Access Token 재발급
    /**
     *
     * @param request -> 클라이언트가 만료된 Access Token을 재발급 받기 위해서 Refresh Token을 보냄.
     * @return
     */
    @PostMapping("/api/auth/refresh")
    @ResponseBody
    public Map<String, String> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        // jwtUtil 사용. Refresh Token에서 사용자 이름 추출.
        String username = jwtUtil.extractUsername(refreshToken);

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

