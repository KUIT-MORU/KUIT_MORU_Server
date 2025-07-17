package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface RoutineTagRepository extends JpaRepository<RoutineTag, UUID> {

    List<RoutineTag> findByRoutine(Routine routine);
    Optional<RoutineTag> findByRoutineAndTag_Id(Routine routine, UUID tagId);
    boolean existsByRoutineAndTag_Id(Routine routine, UUID tagId);
    void deleteByRoutine(Routine routine);

    @Query(value = """
        SELECT rt1.tag_id AS tag1, rt2.tag_id AS tag2, COUNT(*) AS cnt
        FROM routine_tag rt1
        JOIN routine_tag rt2 ON rt1.routine_id = rt2.routine_id AND rt1.tag_id < rt2.tag_id
        GROUP BY rt1.tag_id, rt2.tag_id
        ORDER BY cnt DESC
        LIMIT 10
    """, nativeQuery = true)
    List<TagPairCount> findTopTagPairs();
} 