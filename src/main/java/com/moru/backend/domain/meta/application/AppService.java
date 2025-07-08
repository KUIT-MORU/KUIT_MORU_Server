package com.moru.backend.domain.meta.application;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.domain.App;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppService {
    private final AppRepository appRepository;

    @Transactional
    public App findOrCreateByPackageName(String packageName, String name) {
        return appRepository.findByPackageName(packageName)
                .orElseGet(() -> {
                    App app = App.builder()
                            .name(name)
                            .packageName(packageName)
                            .build();
                    return appRepository.save(app);
                });
    }
}
