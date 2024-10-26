package shop.shop.user.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * JSON Web Token을 생성, 검증, 클레임 추출하는 유틸리티 클래스.
 * 클레임(Clamis) : JWT 내부에 포함된 정보 조각을 의미. 사용자의 신원, 권한, 토큰의 발급 및 만료 시간 등과 같은 데이터를 담고 있음. Payload 부분에 저장된 정보
 */
@Component
public class JwtUtil {

    //JWT의 서명에 사용할 비밀 키를 생성. 토큰 무결성을 확인 할 때 사용된다.
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /*  JWT에서 사용자 이름(주체, Subject)을 추출.
        JWT는 사용자와 관련된 정보를 클레임(Claims)라는 형태로 포함. subject는 토큰의 소유자(여기서는 username)을 나타내는 값이다.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /*
    토큰의 만료 시간을 추출
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Refresh Token 생성 메서드
    // (Refresh Token -> Access Token과 달리 일반적으로 더 긴 유효기간을 가짐. 새로운 Access Token을 발급하는 데 사용된다.)
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 토큰의 주체(username) 설정
                .setIssuedAt(new Date(System.currentTimeMillis())) // 토큰이 발급된 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 토큰 만료일.
                .signWith(secretKey) // 비밀 키를 사용하여 토큰에 서명.
                .compact();
    }
    /*
    특정 클레임을 추출하는 데 사용.
    claimsResolver : 클레임을 어떻게 처리할지를 결정하는 함수형 인터페이스이다.
    extactAllClaims(token) : JWT에서 모든 클레임을 추출하고, 그 중에서 원하는 클레임을 선택적으로 가져온다.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /*
    JWT 토큰을 파싱하여 모든 클레임을 추출한다.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey) // JWT를 파싱할 때 사용할 비밀 키 설정. 토큰을 서명할 때 사용된 키와 동일한 키를 사용하여 토큰의 서명이 유효한지 검증.
                .parseClaimsJws(token) // JWT 토큰을 파싱하고, 서명이 유효한지 확인. 서명이 유효하면 클레임을 반환
                .getBody();
    }

    // 토큰이 만료되었는지 여부 확인.
    // JWT에 포함된 만료 시간(expiration)을 추출한 후, 현재 시간과 비교하여 만료되었는지 확인.
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /*
    Access Token을 생성하는 메서드
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) //토큰의 주체 설정
                .setIssuedAt(new Date(System.currentTimeMillis())) // 토큰 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15))  // 15분 유효한 Access Token
                .signWith(secretKey) // 비밀 키를 사용하여 토큰에 서명
                .compact();
    }

    /*
    JWT가 유효한지 검사
    extractUsername(token) : 토큰에서 추출한 사용자 이름이 주어진 UserDetails의 사용자 이름과 일치하는지 확인
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

