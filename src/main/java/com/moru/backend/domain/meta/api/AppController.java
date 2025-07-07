package com.moru.backend.domain.meta.api;

import com.moru.backend.domain.meta.application.AppService;
import com.moru.backend.domain.meta.dto.AppResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/apps")
@RequiredArgsConstructor
@Tag(name = "앱", description = "앱 관련 API")
public class AppController {

    private final AppService appService;

    // todo : 403 Forbidden 나옴.
    @Operation(summary = "앱 전체 목록 조회", description = "모든 앱의 목록들을 조회")
    @GetMapping
    public ResponseEntity<List<AppResponse>> getAllApps() {
        List<AppResponse> allApps = appService.getAllApps();
        return ResponseEntity.ok(allApps);
    }
}
