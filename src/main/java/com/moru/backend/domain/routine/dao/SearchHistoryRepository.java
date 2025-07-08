package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.search.SearchHistory;
import com.moru.backend.domain.routine.domain.search.SearchType;
import com.moru.backend.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {
    @Query("select sh from SearchHistory sh " +
            "where sh.userId = :userId and sh.searchType = :searchType "+
            "order by sh.createdAt DESC")
    List<SearchHistory> findByUserIdAndSearchTypeOrderByCreatedAtDesc(
            @Param("user") UUID userId,
            @Param("searchType") SearchType searchType
    );

    @Query("select distinct sh.searchKeyword from SearchHistory sh " +
            "where sh.userId = :userId and sh.searchType = :searchType " +
            "order by sh.createdAt DESC"
    )
    List<String> findDistinctKeywordsByUserIdAndSearchType(
            @Param("user") UUID userId,
            @Param("searchType") SearchType searchType
    );

    void deleteByUserIdAndSearchKeyword(UUID userId, String searchKeyword);
}
