package com.moru.backend.domain.routine.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.user.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    List<Routine> findAllByUser(User user);
    int countByUserId(UUID userId);

    /**
     * 검색 기능 관련
     */
    // 최신순으로 정렬된
    @Query("select distinct r from Routine r " +
            "left join r.routineTags rt " +
            "where (:titleKeyword is null or r.title like %:titleKeyword%) " +
            "and (:tagNames is null or rt.tag.name in :tagNames) " +
            "order by r.createdAt desc")
    Page<Routine> findBySearchCriteriaOrderByCreatedAt(
            @Param("titleKeyword") String titleKeyword,
            @Param("tagNames") List<String> tagNames,
            Pageable pageable);


    // 인기순으로 정렬된
    @Query("select distinct r from Routine r " +
            "left join r.routineTags rt " +
            "where (:titleKeyword is null or r.title like %:titleKeyword%) " +
            "and (:tagNames is null or rt.tag.name in :tagNames) " +
            "order by r.likeCount desc, r.createdAt desc")
    Page<Routine> findBySearchCriteriaOrderByLikeCount(
            @Param("titleKeyword") String titleKeyword,
            @Param("tagNames") List<String> tagNames,
            Pageable pageable
    );

    // 루틴명 자동완성
    @Query("select distinct r.title from Routine r " +
            "where r.title like %:keyword% " +
            "order by r.title")
    List<String> findTitleSuggestions(@Param("keyword") String keyword);
}
