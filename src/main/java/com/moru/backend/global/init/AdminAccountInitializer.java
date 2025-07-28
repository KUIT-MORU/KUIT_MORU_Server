package com.moru.backend.global.init;

import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminAccountInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createAdminUser() {
        return args -> {
            String adminEmail = "admin@example.com";

            if (userRepository.existsByEmail(adminEmail)) {
                System.out.println("ℹ️ 이미 관리자 계정이 존재합니다: " + adminEmail);
                return;
            }

            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin1234")) // 나중에 환경변수로 바꾸는 걸 추천
                    .role(UserRole.ADMIN)
                    .nickname("관리자")
                    .build();

            userRepository.save(admin);
            System.out.println("✅ 관리자 계정이 생성되었습니다: " + adminEmail);
        };
    }
}

