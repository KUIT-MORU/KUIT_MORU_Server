package com.moru.backend.domain.meta.dao;

import com.moru.backend.domain.meta.domain.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
     Optional<App> findByPackageName(String packageName);
     boolean existsByPackageName(String packageName);
}
