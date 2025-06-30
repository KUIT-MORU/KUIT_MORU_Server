package com.moru.backend.domain.meta.dao;

import com.moru.backend.domain.meta.domain.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
} 