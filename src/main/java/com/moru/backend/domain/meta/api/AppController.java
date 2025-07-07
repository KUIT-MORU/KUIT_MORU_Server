package com.moru.backend.domain.meta.api;

import com.moru.backend.domain.meta.dto.request.InstalledAppsRequest;
import com.moru.backend.domain.meta.dto.request.SelectedAppsRequest;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/apps")
@RequiredArgsConstructor
@Tag(name = "앱", description = "앱 관련 API")
public class AppController {
    private final AppService appService;

    @Operation(summary = "앱 ID (클라이언트에서 관리하는 앱이므로 항상 null)", description = "안드로이드에서 전송한 설치된 앱 목록을 처리한다")
    @PostMapping("/installed")
    public ResponseEntity<List<InstalledAppResponse>> processInstalledApps(
            @CurrentUser User user,
            @Valid @RequestBody InstalledAppsRequest request
            ) {
        List<InstalledAppResponse> apps = appService.processInstalledApps(request);
        return ResponseEntity.ok(apps);
    }

    @Operation(summary = "선택된 앱들 검증", description = "선택된 앱들을 검증하고 응답합니다.")
    @PostMapping("/apps/selected")
    public ResponseEntity<SelectedAppsResponse> validateSelectedApps(
            @CurrentUser User user,
            @Valid @RequestBody SelectedAppsRequest request) {
        SelectedAppsResponse response = appService.validateSelectedApps(request);
        return ResponseEntity.ok(response);
    }

    private boolean isValidPackageName(String packageName) {
        return packageName != null &&
                packageName.matches("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$");
    }
}
