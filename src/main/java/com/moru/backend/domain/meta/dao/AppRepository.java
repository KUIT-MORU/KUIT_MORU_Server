package com.moru.backend.domain.meta.dao;

import com.moru.backend.domain.meta.domain.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
    @Query("select a from App a order by a.name")
    List<App> findAllOrderByName();
} 