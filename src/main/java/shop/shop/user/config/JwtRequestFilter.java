package shop.shop.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import shop.shop.user.service.CustomUserDetailsService;

import java.io.IOException;

@Component //빈 등록
@RequiredArgsConstructor //final로 선언된 필드를 포함한 생성자를 자동으로 생성
//OncePerRequestFilter : 모든 HTTP 요청마다 한 번만 실행되는 필터를 의미. Spring Security에서는 각 요청에 대해 이 필터가 호출된다.
@Log4j2
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();

        // loginPage와 api/auth/login 경로에 대해서는 필터링하지 않음
        if (requestURI.equals("/loginPage") || requestURI.startsWith("/api/auth")) {
            chain.doFilter(request, response);
            return;
        }
        //Authorization 헤더를 통해 클라이언트가 전달한 JWT 토큰 추출.
        final String authorizationHeader = request.getHeader("Authorization");
        log.info("Authorization 헤더1: {}", authorizationHeader); // 토큰 출력 확인
        String username = null;
        String jwt = null;

        //헤더가 존재하고 Bearer 로 시작하는 경우, Bearer 뒤에 있는 JWT 토큰을 추출. (Bearer <JWT 토큰>의 형태로 전달되는 토큰에서 <JWT 토큰>만 추출)
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            //사용자 이름(username) 추출
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.error("JWT 토큰 추출 중 오류 발생: " + e.getMessage());
            }
        }
        /**
         * 사용자 이름(username)이 존재하고, 현재 SecurityContext에 이미 인증 정보가 없다면, JWT 토큰을 검증하고 사용자 정보를 로드하여 인증을 처리
         *
         */
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //customUserDetailsService.loadUserByUsername(username) : 데이터베이스에서 해당 사용자의 세부 정보(UserDetails) 가져오기.
            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

            //토큰이 유효하다면 인증이 진행.
            if (jwtUtil.validateToken(jwt, userDetails)) {
                //UsernamePasswordAuthenticationToken : 이 객체는 스프링 시큐리티에서 인증된 사용자를 나타낸다.
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                //스프링 시큐리티의 SecurityContext에 인증 정보를 저장. 이를 통해 이후의 요청에서 인증된 사용자로 인식된다.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                log.info("인증 성공, 사용자: {}", username);
                log.info("Authentication 설정됨: {}", usernamePasswordAuthenticationToken);
            }else {
                log.warn("JWT 토큰이 유효하지 않습니다.");
            }
        }
        chain.doFilter(request, response);

        log.info("Authorization 헤더: {}", authorizationHeader);
        log.info("JWT 토큰: {}", jwt);
        log.info("사용자 이름: {}", username);
    }
}

