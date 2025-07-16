package com.moru.backend.domain.routine.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.user.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    List<Routine> findAllByUser(User user);
    int countByUserId(UUID userId);

    /**
     * 루틴 ID로 루틴과 관련된 스텝, 태그, 앱을 함께 조회
     */
    @Query("SELECT r FROM Routine r " +
            "LEFT JOIN FETCH r.routineSteps " +
            "LEFT JOIN FETCH r.routineTags t LEFT JOIN FETCH t.tag " +
            "LEFT JOIN FETCH r.routineApps " +
            "WHERE r.id = :id")
    Optional<Routine> findByRoutineIdWithStepsTagsApps(@Param("id") UUID routineId);

    /**
     * 루틴 ID로 루틴과 관련된 태그, 앱을 함께 조회
     */
    @Query("SELECT r FROM Routine r " +
            "LEFT JOIN FETCH r.routineTags t LEFT JOIN FETCH t.tag " +
            "LEFT JOIN FETCH r.routineApps " +
            "WHERE r.id = :id")
    Optional<Routine> findByRoutineIdWithTagsApps(@Param("id") UUID routineId);

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

    /**
     * 내 루틴 정렬
     * @param userId
     * @param dayOfWeek
     * @param pageable
     * @return
     */
    // 시간순으로 정렬된
    @Query("SELECT r FROM Routine r JOIN r.routineSchedules s WHERE r.user.id = :userId AND s.dayOfWeek = :dayOfWeek ORDER BY s.time ASC")
    Page<Routine> findByUserIdAndDayOfWeekOrderByScheduleTimeAsc(@Param("userId") UUID userId, @Param("dayOfWeek") DayOfWeek dayOfWeek, Pageable pageable);

    @Query("select distinct r from Routine r left join r.routineTags rt where r.user = :user order by r.likeCount desc, r.createdAt desc")
    Page<Routine> findByUserOrderByLikeCountDescCreatedAtDesc(@Param("user") User user, Pageable pageable);

    @Query("select distinct r from Routine r left join r.routineTags rt where r.user = :user order by r.createdAt desc")
    Page<Routine> findByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    // 지금 가장 핫한 루틴틴
    @Query("SELECT r FROM Routine r WHERE r.createdAt >= :weekAgo ORDER BY (r.viewCount * 0.5 + r.likeCount * 0.5) DESC, r.createdAt DESC")
    List<Routine> findHotRoutines(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);

    @Query("SELECT r FROM Routine r JOIN r.routineTags rt WHERE rt.tag.name IN :tags GROUP BY r.id ORDER BY COUNT(rt.tag.name) DESC, r.createdAt DESC")
    List<Routine> findRoutinesByTagsOrderByTagCount(@Param("tags") List<String> tags, Pageable pageable);
}
