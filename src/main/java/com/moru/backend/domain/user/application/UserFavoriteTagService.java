package com.moru.backend.domain.user.application;

import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.user.dao.UserFavoriteTagRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.domain.UserFavoriteTag;
import com.moru.backend.domain.user.dto.FavoriteTagRequest;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFavoriteTagService {

    private final TagRepository tagRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;

    @Transactional
    public void addFavoriteTag(User user, FavoriteTagRequest request) {
        for(UUID tagId : request.tagIds()) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));

            boolean alreadyExists = userFavoriteTagRepository.existsByUserIdAndTagId(user.getId(), tagId);
            if(alreadyExists) {
                throw new CustomException(ErrorCode.TAG_ALREAEDY_EXISTS);
            }

            UserFavoriteTag favoriteTag = UserFavoriteTag.builder()
                    .user(user)
                    .tag(tag)
                    .build();

            userFavoriteTagRepository.save(favoriteTag);
        }
    }

    @Transactional
    public void removeFavoriteTag(User user, UUID tagId) {
        UserFavoriteTag userFavoriteTag = userFavoriteTagRepository.findByUserIdAndTagId(user.getId(), tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));
        userFavoriteTagRepository.delete(userFavoriteTag);
    }
}
