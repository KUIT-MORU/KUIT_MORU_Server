package com.moru.backend.domain.Tag.dao;

import com.moru.backend.domain.Tag.domain.Tag;

import jakarta.validation.constraints.Size;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String tagName);

} 