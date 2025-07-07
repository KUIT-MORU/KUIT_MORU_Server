package com.moru.backend.global.jwt.filter;

import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.jwt.JwtProvider;
import com.moru.backend.global.redis.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = jwtProvider.extractAccessToken(request);

        // AccessToken 만료 시 RefreshToken으로 재발급
        if(token != null && jwtProvider.isTokenExpired(token)) {
            try {
                UUID userId = jwtProvider.getSubject(token);

                // 유효한 유저(soft deleted 여부)인지 확인
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                if(!user.isActive()) {
                    throw new CustomException(ErrorCode.USER_DEACTIVATED);
                }

                String refreshToken = refreshTokenRepository.get(userId.toString());

                if(refreshToken != null && jwtProvider.validateToken(refreshToken)) {
                    // AccessToken 및 RefreshToken 재발급
                    String newAccessToken = jwtProvider.createAccessToken(userId);
                    String newRefreshToken = jwtProvider.createRefreshToken(userId);

                    // Redis에 RefreshToken 갱신
                    refreshTokenRepository.save(userId.toString(), newRefreshToken);

                    // 응답 헤더
                    response.setHeader("Authorization", "Bearer " + newAccessToken);
                    response.setHeader("X-Refresh-Token", newRefreshToken);

                    // SecurityContext 갱신
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 인증 실패, 인증 정보 삭제
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                // 인증 실패, 인증 정보 삭제
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        // AccessToken 유효
        } else if(token != null && jwtProvider.validateToken(token)) {
            UUID userId = jwtProvider.getSubject(token);

            // 유효한 유저(soft deleted 여부)인지 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            if(!user.isActive()) {
                throw new CustomException(ErrorCode.USER_DEACTIVATED);
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}