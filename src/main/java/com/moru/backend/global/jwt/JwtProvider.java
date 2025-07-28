package com.moru.backend.global.jwt;

import com.moru.backend.domain.user.domain.UserRole;
import com.moru.backend.global.config.JwtProperties;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import io.jsonwebtoken.Claims;

@Component
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private Key secretKey;

    private final long accessTokenValidity = 1000 * 60 * 60; // 1hour

    @Getter
    private final long refreshTokenValidity = 1000 * 60 * 60 * 24; // 1 day

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        // application.yml에 설정한 "secret" 값을 암호화 키로 변환해서 JWT 서명에 사용할 수 있게 만드는 것
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String createAccessToken(UUID userId, UserRole role) {
        return createToken(userId, role, accessTokenValidity);
    }

    public String createRefreshToken(UUID userId, UserRole role) {
        return createToken(userId, role, refreshTokenValidity);
    }

    private String createToken(UUID userId, UserRole role, long validity) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID getSubject(String token) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return UUID.fromString(subject);
        } catch (Exception e) {
            // 유효하지 않은 토큰
            return null;
        }
    }

    public UserRole getRole(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN); // 또는 INVALID_ROLE
        }

        String roleStr = claims.get("role", String.class);
        if (roleStr == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN); // role 누락된 토큰
        }

        try {
            return UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ROLE); // 정의되지 않은 ROLE
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public String extractAccessToken(HttpServletRequest request) {
        String authrizationHeader = request.getHeader("Authorization");
        if(authrizationHeader != null && authrizationHeader.startsWith("Bearer ")) {
            return authrizationHeader.substring(7);
        }
        return null;
    }

    public boolean isTokenExpired(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
