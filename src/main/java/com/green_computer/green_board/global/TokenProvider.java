package com.green_computer.green_board.global;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class TokenProvider {
    private final Key key;

    // 엑세스 토큰의 지속 시간 (밀리초)
    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 60;

    // 리프레시 토큰의 지속 시간 (밀리초)
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24;

    public TokenProvider(@Value("${spring.jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 엑세스 토큰 생성
    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim("tokenType", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .claim("tokenType", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성(위/변조 여부) 검증
    public boolean validateAccessToken(String token) {
        try{
            String tokenType = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("tokenType", String.class);
            return "ACCESS".equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try{
            String tokenType = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("tokenType", String.class);
            return "REFRESH".equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Jwt에서 username을 다시 추출
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Date getExpiration(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
