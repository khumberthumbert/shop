package shop.shop.jwt;

import io.jsonwebtoken.Jwts;
import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
@Log4j2
public class JWTUtil {

    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    /*
    검증
     */
    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }


    /*
    토큰 생성 메서드
     */
    public String createJwt(String username, String role, Long expiredMs) {
        // 발행 시간과 만료 시간 계산
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(System.currentTimeMillis() + expiredMs);

        // 로그로 출력
        log.info("JWT Issued At: {}", issuedAt);
        log.info("JWT Expiry Time: {}", expiryDate);

        // JWT 생성 및 반환
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }


}
