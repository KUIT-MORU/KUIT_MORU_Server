package com.moru.backend.domain.log.dto;

public record LiveUserResponse(
    String username,
    String profileImageUrl,
    String motivationTag,
    // 개인 페이지로 이동하기 용 url 
    String feelUrl
) {} 