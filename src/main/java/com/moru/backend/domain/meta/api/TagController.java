package com.moru.backend.domain.meta.api;

import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.meta.dto.response.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "태그", description = "시스템 내장 태그 전체 목록 조회 API")
public class TagController {
    private final TagRepository tagRepository;

    @Operation(summary = "태그 전체 목록 조회", description = "시스템에 내장된 모든 태그를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTag() {
        List<Tag> tags = tagRepository.findAll();
        List<TagResponse> response = tags.stream()
                .map(TagResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}