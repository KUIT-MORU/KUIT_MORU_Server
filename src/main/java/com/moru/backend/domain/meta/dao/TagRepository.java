package com.moru.backend.domain.meta.dao;

import com.moru.backend.domain.meta.domain.Tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String tagName);

} 