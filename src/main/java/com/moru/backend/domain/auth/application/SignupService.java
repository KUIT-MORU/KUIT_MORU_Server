package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.SignupRequest;
import com.moru.backend.domain.user.application.UserFavoriteTagService;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.dto.FavoriteTagRequest;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.util.S3Directory;
import com.moru.backend.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserFavoriteTagService userFavoriteTagService;
    private final S3Service s3Service;

    @Transactional
    public void signup(SignupRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
        if(userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
        }

        // === 이미지 이동 처리 ===
        String imageKey = null;
        if(request.profileImageUrl() != null && !request.profileImageUrl().isBlank()) {
            imageKey = s3Service.moveToRealLocation(request.profileImageUrl(), S3Directory.PROFILE);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .gender(request.gender())
                .birthday(request.birthday())
                .bio(request.bio())
                .profileImageUrl(imageKey)
                .status(true)
                .build();
        userRepository.save(user);

        // 관심 태그 저장
        if(request.tagIds() != null && !request.tagIds().isEmpty()) {
            FavoriteTagRequest tagRequest = new FavoriteTagRequest(request.tagIds());
            userFavoriteTagService.addFavoriteTag(user, tagRequest);
        }
    }
}
