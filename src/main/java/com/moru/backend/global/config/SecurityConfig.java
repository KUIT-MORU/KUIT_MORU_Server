package com.moru.backend.global.config;

import com.moru.backend.domain.auth.application.TokenBlacklistService;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.global.jwt.JwtProvider;
import com.moru.backend.global.jwt.filter.JwtAuthenticationFilter;
import com.moru.backend.global.redis.RefreshTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityConfig(JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, TokenBlacklistService tokenBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/auth/**",
                            "/api/user/nickname",
                            "/login",
                            "/",
                            "/error/**",
                            "/favicon.ico",
                            "/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/swagger-ui.html",
                            "/webjars/**",
                            "/api-docs"
                    ).permitAll()
                    .requestMatchers("/routines/**").authenticated() // 루틴 API는 인증 필요
                    .anyRequest().authenticated() // 그 외에는 인증 필요
            )
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, refreshTokenRepository, userRepository, tokenBlacklistService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
