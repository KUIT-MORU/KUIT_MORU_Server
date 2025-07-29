package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface RoutineTagRepository extends JpaRepository<RoutineTag, UUID> {

    List<RoutineTag> findByRoutine(Routine routine);
    Optional<RoutineTag> findByRoutineAndTag_Id(Routine routine, UUID tagId);
    boolean existsByRoutineAndTag_Id(Routine routine, UUID tagId);
    void deleteByRoutine(Routine routine);

    @Query(value = """
        SELECT BIN_TO_UUID(rt1.tag_id) AS tag1, BIN_TO_UUID(rt2.tag_id) AS tag2, COUNT(*) AS cnt
        FROM routine_tag rt1
        JOIN routine_tag rt2 ON rt1.routine_id = rt2.routine_id AND rt1.tag_id < rt2.tag_id
        GROUP BY rt1.tag_id, rt2.tag_id
        ORDER BY cnt DESC
        LIMIT 10
    """, nativeQuery = true)
    List<TagPairCount> findTopTagPairs();

    @Query(value = """
         SELECT BIN_TO_UUID(rt1.tag_id) AS tag1, BIN_TO_UUID(rt2.tag_id) AS tag2, COUNT(*) AS cnt
         FROM routine_tag rt1
         JOIN routine_tag rt2 ON rt1.routine_id = rt2.routine_id AND rt1.tag_id < rt2.tag_id
         WHERE rt1.tag_id IN (:interestTagIds) OR rt2.tag_id IN (:interestTagIds)
         GROUP BY rt1.tag_id, rt2.tag_id
         ORDER BY cnt DESC
         LIMIT 10
     """, nativeQuery = true)
    List<TagPairCount> findTopTagPairsForInterests(@Param("interestTagIds") List<UUID> interestTagIds);

    // RoutineTagRepository.java 인터페이스에 아래 두 메서드를 추가하세요.
    @Query("SELECT t.name FROM RoutineTag rt JOIN rt.tag t WHERE rt.routine.user = :user")
    List<String> findTagNamesByUserRoutines(@Param("user") User user);

    @Query("SELECT t.name FROM RoutineUserAction rua JOIN rua.routine.routineTags rt JOIN rt.tag t WHERE rua.user = :user AND rua.actionType = 'SCRAP'")
    List<String> findTagNamesByUserScrappedRoutines(@Param("user") User user);
} 