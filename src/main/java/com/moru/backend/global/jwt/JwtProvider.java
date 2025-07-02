package com.moru.backend.global.jwt;

import com.moru.backend.global.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private Key secretKey;

    private final long accessTokenValidity = 1000 * 60 * 60; // 1hour
    private final long refreshTokenValidity = 1000 * 60 * 60 * 24; // 1 day

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        // application.yml에 설정한 "secret" 값을 암호화 키로 변환해서 JWT 서명에 사용할 수 있게 만드는 것
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String createAccessToken(UUID userId) {
        return createToken(userId, accessTokenValidity);
    }

    public String createRefreshToken(UUID userId) {
        return createToken(userId, refreshTokenValidity);
    }

    private String createToken(UUID userId, long validity) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
