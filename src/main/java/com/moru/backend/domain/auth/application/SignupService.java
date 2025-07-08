package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.SignupRequest;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.user.dao.UserFavoriteTagRepository;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.domain.UserFavoriteTag;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagRepository tagRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;

    @Transactional
    public void signup(SignupRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
        if(userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .gender(request.gender())
                .birthday(request.birthday())
                .bio(request.bio())
                .profileImageUrl(request.profileImageUrl())
                .status(true)
                .build();
        userRepository.save(user);

        // 관심 태그 저장
        if(request.tagIds() != null && !request.tagIds().isEmpty()) {
            List<UserFavoriteTag> favoriteTags = request.tagIds().stream()
                    .map(tagId -> UserFavoriteTag.builder()
                            .user(user)
                            .tag(tagRepository.getReferenceById(tagId))
                            .build()
                    )
                    .toList();
            userFavoriteTagRepository.saveAll(favoriteTags);
        }
    }
}
