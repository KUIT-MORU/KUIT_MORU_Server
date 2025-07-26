package com.moru.backend.domain.user.application;

import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String getNicknameById(UUID userId) {
        return userRepository.findNicknameById(userId);
    }

    public String getProfileImageUrlById(UUID userId) {
        return userRepository.findProfileImageUrlById(userId);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElse(null);
    }
}
