package com.moru.backend.domain.routine.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.user.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    List<Routine> findAllByUser(User user);
    int countByUserId(UUID userId);
    @Query("""
             SELECT DISTINCT r FROM Routine r
             LEFT JOIN FETCH r.routineTags rt
             LEFT JOIN FETCH rt.tag
             LEFT JOIN FETCH r.routineSteps
             LEFT JOIN FETCH r.routineApps ra
             LEFT JOIN FETCH ra.app
             WHERE r.id = :routineId
             """)
    Optional<Routine> findByIdWithDetails(@Param("routineId") UUID routineId);

    /**
     * 검색 기능 관련
     */
    // 최신순으로 정렬된
    @Query(value = "SELECT r.id FROM Routine r LEFT JOIN r.routineTags rt " +
            "WHERE (:titleKeyword IS NULL OR r.title LIKE %:titleKeyword%) " +
            "AND (:tagNames IS NULL OR rt.tag.name IN :tagNames) " +
            "GROUP BY r.id ORDER BY r.createdAt DESC",
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM Routine r LEFT JOIN r.routineTags rt " +
                    "WHERE (:titleKeyword IS NULL OR r.title LIKE %:titleKeyword%) " +
                    "AND (:tagNames IS NULL OR rt.tag.name IN :tagNames)")
    Page<UUID> findIdsBySearchCriteriaOrderByCreatedAt(
            @Param("titleKeyword") String titleKeyword,
            @Param("tagNames") List<String> tagNames,
            Pageable pageable);


    // 인기순으로 정렬된
    @Query(value = "SELECT r.id FROM Routine r LEFT JOIN r.routineTags rt " +
            "WHERE (:titleKeyword IS NULL OR r.title LIKE %:titleKeyword%) " +
            "AND (:tagNames IS NULL OR rt.tag.name IN :tagNames) " +
            "GROUP BY r.id ORDER BY r.likeCount DESC, r.createdAt DESC",
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM Routine r LEFT JOIN r.routineTags rt " +
                    "WHERE (:titleKeyword IS NULL OR r.title LIKE %:titleKeyword%) " +
                    "AND (:tagNames IS NULL OR rt.tag.name IN :tagNames)")
    Page<UUID> findIdsBySearchCriteriaOrderByLikeCount(
            @Param("titleKeyword") String titleKeyword,
            @Param("tagNames") List<String> tagNames,
            Pageable pageable
    );
 

    // 루틴명 자동완성
    @Query("SELECT DISTINCT r.title FROM Routine r WHERE r.title LIKE %:keyword% ORDER BY r.title")
    List<String> findTitleSuggestions(@Param("keyword") String keyword);

    /**
     * 내 루틴 정렬
     * @param userId    조회할 사용자의 ID
     * @param dayOfWeek 필터링할 요일 (e.g., MON, TUE)
     * @param pageable  페이징 및 정렬 정보
     * @return 조건에 맞는 루틴의 페이징된 목록
     */
    // 시간순으로 정렬된
    @Query(value = "SELECT r.id FROM Routine r JOIN r.routineSchedules s WHERE r.user.id = :userId AND s.dayOfWeek = :dayOfWeek GROUP BY r.id, s.time ORDER BY s.time ASC",
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM Routine r JOIN r.routineSchedules s WHERE r.user.id = :userId AND s.dayOfWeek = :dayOfWeek")
    Page<UUID> findIdsByUserIdAndDayOfWeekOrderByScheduleTimeAsc(@Param("userId") UUID userId, @Param("dayOfWeek") DayOfWeek dayOfWeek, Pageable pageable);

    @Query(value = "SELECT r.id FROM Routine r WHERE r.user = :user GROUP BY r.id ORDER BY r.likeCount DESC, r.createdAt DESC",
            countQuery = "SELECT COUNT(r.id) FROM Routine r WHERE r.user = :user")
    Page<UUID> findIdsByUserOrderByLikeCountDescCreatedAtDesc(@Param("user") User user, Pageable pageable);

    @Query(value = "SELECT r.id FROM Routine r WHERE r.user = :user GROUP BY r.id ORDER BY r.createdAt DESC",
            countQuery = "SELECT COUNT(r.id) FROM Routine r WHERE r.user = :user")
    Page<UUID> findIdsByUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    List<Routine> findAllByUserId(UUID userId);

    //====루틴 추천 정렬 기능====// 
    /**
     * 최근 일주일간 생성된 루틴 중 조회수와 좋아요 수를 가중치로 계산하여 인기순으로 정렬합니다.
     *
     * @param weekAgo    조회 시작 시점 (일주일 전)
     * @param viewWeight 조회수 가중치
     * @param likeWeight 좋아요 수 가중치
     * @param pageable   결과 개수 제한
     * @return 인기 루틴 목록
     */
    @Query(value = "SELECT r.id FROM Routine r WHERE r.createdAt >= :weekAgo " +
            "GROUP BY r.id, r.viewCount, r.likeCount, r.createdAt " + // GROUP BY에 정렬 기준 컬럼 추가
            "ORDER BY (CAST(r.viewCount AS double) * :viewWeight + CAST(r.likeCount AS double) * :likeWeight) DESC, r.createdAt DESC")
    List<UUID> findHotRoutinesIds(@Param("weekAgo") LocalDateTime weekAgo,
                                  @Param("viewWeight") double viewWeight,
                                  @Param("likeWeight") double likeWeight,
                                  Pageable pageable);

    /**
     * 주어진 태그를 포함하는 루틴의 ID를, 태그 일치 개수가 많은 순으로 정렬하여 조회.
     * JOIN FETCH를 제거하여 COUNT 및 GROUP BY와 함께 사용할 수 있도록 수정했.
     */
    @Query(value = "SELECT r.id FROM Routine r JOIN r.routineTags rt WHERE rt.tag.name IN :tags GROUP BY r.id ORDER BY COUNT(r.id) DESC, r.createdAt DESC",
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM Routine r JOIN r.routineTags rt WHERE rt.tag.name IN :tags")
    Page<UUID> findRoutineIdsByTagsOrderByTagCount(@Param("tags") List<String> tags, Pageable pageable);

    @Query("""
        SELECT r FROM Routine r
        LEFT JOIN FETCH r.user
        LEFT JOIN FETCH r.routineTags rt LEFT JOIN FETCH rt.tag
        LEFT JOIN FETCH r.routineSteps
        LEFT JOIN FETCH r.routineApps ra LEFT JOIN FETCH ra.app
        WHERE r.id IN :ids
    """)
    List<Routine> findWithAllDetailsByIds(@Param("ids") List<UUID> ids);
    /**
     * 두 개의 태그를 모두 포함하는 루틴을 인기순으로 정렬하여 조회합니다.
     *
     * @param tag1     첫 번째 태그 ID
     * @param tag2     두 번째 태그 ID
     * @param pageable 페이징 정보
     * @return 태그 쌍 관련 루틴 목록
     */
    @Query(value = """
        SELECT r.id FROM Routine r
        JOIN r.routineTags rt1
        JOIN r.routineTags rt2
        WHERE rt1.tag.id = :tag1 AND rt2.tag.id = :tag2
        GROUP BY r.id
        ORDER BY (CAST(r.viewCount AS double) * 0.5 + CAST(r.likeCount AS double) * 0.5) DESC, r.createdAt DESC
    """, countQuery = "SELECT COUNT(DISTINCT r.id) FROM Routine r JOIN r.routineTags rt1 JOIN r.routineTags rt2 WHERE rt1.tag.id = :tag1 AND rt2.tag.id = :tag2")
    Page<UUID> findRoutineIdsByTagPair(@Param("tag1") UUID tag1, @Param("tag2") UUID tag2, Pageable pageable);

    /**
     * 특정 루틴의 조회수를 1 증가시킵니다.
     *
     * @param id 조회수를 증가시킬 루틴의 ID
     */
    @Modifying
    @Query("update Routine r set r.viewCount = r.viewCount + 1 where r.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    /**
     * 특정 루틴과 비슷한 태그를 가진 다른 루틴들을 추천합니다.
     *
     * @param tagIds    기준 루틴이 가진 태그 ID 목록
     * @param routineId 기준 루틴의 ID (결과에서 제외하기 위함)
     * @param pageable  페이징 정보
     * @return 유사 루틴 목록
     */
    @Query(value = """
         SELECT r.id FROM Routine r
         JOIN r.routineTags rt
         WHERE rt.tag.id IN :tagIds AND r.id <> :routineId
         GROUP BY r.id
         ORDER BY COUNT(rt.tag.id) DESC, r.createdAt DESC
    """, countQuery = "SELECT COUNT(DISTINCT r.id) FROM Routine r JOIN r.routineTags rt WHERE rt.tag.id IN :tagIds AND r.id <> :routineId")
    Page<UUID> findSimilarRoutineIdsByTagIds(@Param("tagIds") List<UUID> tagIds, @Param("routineId") UUID routineId, Pageable pageable);


    @Query("SELECT r.title FROM Routine r WHERE r.id = :routineId")
    String findTitleById(@Param("routineId") UUID routineId);

    @Query("SELECT r.isUserVisible FROM Routine r WHERE r.id = :routineId")
    boolean getIsUserVisibleById(@Param("routineId") UUID routineId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Routine r set r.likeCount = r.likeCount + 1 where r.id = :id")
    int incrementLikeCount(@Param("id") UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Routine r
              set r.likeCount = case when r.likeCount > 0 then r.likeCount - 1 else 0 end
            where r.id = :id
           """)
    int decrementLikeCount(@Param("id") UUID id);

}
