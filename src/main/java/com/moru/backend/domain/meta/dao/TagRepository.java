package com.moru.backend.domain.meta.dao;

import com.moru.backend.domain.meta.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String tagName);

    @Query("select t from Tag t where t.name like %:keyword% order by t.name")
    List<Tag> findByNameContainingOrderByName(@Param("keyword") String keyword);
}