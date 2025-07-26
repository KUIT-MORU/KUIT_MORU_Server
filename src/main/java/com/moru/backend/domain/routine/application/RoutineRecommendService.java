package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.dao.TagPairCount;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.dto.response.RecommendFeedResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.routine.dto.response.TagPairSection;
import com.moru.backend.domain.user.dao.UserFavoriteTagRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
// feed_main
public class RoutineRecommendService {
    private final RoutineRepository routineRepository;
    private final RoutineTagRepository routineTagRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;
    private final TagRepository tagRepository;
    private final S3Service s3Service;

    @Value("${moru.routine.recommend.hot-score.view-weight}")
    private double viewWeight;

    @Value("${moru.routine.recommend.hot-score.like-weight}")
    private double likeWeight;

    public RecommendFeedResponse getRecommendFeed(User user) {
        List<RoutineListResponse> hotRoutines = getHotRoutines(10);
        List<RoutineListResponse> personalRoutines = getPersonalRoutines(user, 10);
        TagPairSection tagPairSection1 = getTopTagPairSection(10);
        TagPairSection tagPairSection2 = getInterestTagPairSection(user, 10);

        return new RecommendFeedResponse(hotRoutines, personalRoutines, tagPairSection1, tagPairSection2);
    }

    /**
     * 지금 가장 핫한 루틴
     */
    public List<RoutineListResponse> getHotRoutines(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return routineRepository.findHotRoutines(weekAgo, viewWeight, likeWeight, PageRequest.of(0, limit)).stream()
                .map(this::toRoutineListResponse)
                .toList();
    }

    /**
     * 개인화 추천
     * 1순위 : 사용자의 관심 태그와 사용자가 만든 내 루틴의 태그를 기반으로 추천
     * 2순위 : 충분한 루틴이 생성되지 않으면 -> 사용자가 스크랩한 루틴의 태그를 기반으로 추가 추천
     * 3순위 : 그래도 부족하면, 일반적으로 인기 있는 핫 루틴으로 나머지 목록 채운다.
     */
    public List<RoutineListResponse> getPersonalRoutines(User user, int limit) {
        // 추천 루틴 중복 방지를 위한 Set
        Set<UUID> recommendedRoutineIds = new HashSet<>();
        List<RoutineListResponse> result = new ArrayList<>();

        // N+1 문제 해결 -> Repository 메서드 새로 생성
        List<String> primaryTags = Stream.concat(
                userFavoriteTagRepository.findFavoriteTagNamesByUserId(user.getId()).stream(),
                routineTagRepository.findTagNamesByUserRoutines(user).stream()
        ).toList();
        List<String> secondaryTags = routineTagRepository.findTagNamesByUserScrappedRoutines(user);

        fillRoutinesFromTags(primaryTags, recommendedRoutineIds, result, limit);

        // 필요시에 2차 태그로 추천 목록 채우기
        if (result.size() < limit) {
            fillRoutinesFromTags(secondaryTags, recommendedRoutineIds, result, limit);
        }

        // 그래도 부족하면 인기 루틴으로 채우기
        if (result.size() < limit) {
            fillWithHotRoutines(recommendedRoutineIds, result, limit);
        }

        return result;
    }

    /**
     * 태그 조합 추천
     * 함께 자주 사용되는 태그 조합을 찾아 해당 루틴 보여준다.
     */
    public TagPairSection getTopTagPairSection(int limit) {
        List<TagPairCount> topPairs = routineTagRepository.findTopTagPairs();
        if (topPairs.isEmpty()) {
            return null;
        }
        // Stream API를 사용하여 더 간결하게 표현
        return topPairs.stream()
                .map(pair -> buildTagPairSection(pair, limit))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public TagPairSection getInterestTagPairSection(User user, int limit) {
        Set<String> interestTagIds = userFavoriteTagRepository.findAllByUserId(user.getId()).stream()
                .map(f -> f.getTag().getId().toString())
                .collect(Collectors.toSet());

        if (interestTagIds.isEmpty()) {
            return null;
        }

        List<TagPairCount> topPairs = routineTagRepository.findTopTagPairs();
        // Stream API를 사용하여 더 간결하게 표현
        return topPairs.stream()
                .filter(pair -> interestTagIds.contains(pair.getTag1()) || interestTagIds.contains(pair.getTag2()))
                .map(pair -> buildTagPairSection(pair, limit))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }


    // ========================= 유틸/헬퍼 ========================= //
    /**
     * 주어진 태그 목록을 기반으로 추천 루틴 목록을 채우고, 중복 로직을 추출함.
     */
    private void fillRoutinesFromTags(List<String> tagNames, Set<UUID> existingIds, List<RoutineListResponse> result, int limit) {
        if (tagNames.isEmpty() || result.size() >= limit) {
            return;
        }

        List<String> sortedTags = sortTagsByFrequency(tagNames);
        List<Routine> routines = routineRepository.findRoutinesByTagsOrderByTagCount(sortedTags, PageRequest.of(0, limit));

        for (Routine routine : routines) {
            if (result.size() >= limit) break;
            if (existingIds.add(routine.getId())) {
                result.add(toRoutineListResponse(routine));
            }
        }
    }

    private List<String> sortTagsByFrequency(List<String> tagNames) {
        return tagNames.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    private void fillWithHotRoutines(Set<UUID> existingIds, List<RoutineListResponse> results, int limit) {
        if (results.size() >= limit) {
            return;
        }

        // 중복을 고려하여 넉넉하게 조회
        List<RoutineListResponse> hotRoutines = getHotRoutines(limit * 2);
        for (RoutineListResponse routine : hotRoutines) {
            if (results.size() >= limit) break;
            if (existingIds.add(routine.id())) {
                results.add(routine);
            }
        }
    }

    private TagPairSection buildTagPairSection(TagPairCount pair, int limit) {
        if (pair == null || pair.getTag1() == null || pair.getTag2() == null) {
            return null;
        }
        try {
            UUID tagId1 = UUID.fromString(pair.getTag1());
            UUID tagId2 = UUID.fromString(pair.getTag2());

            // 두 태그를 한 번의 쿼리로 조회하여 Map으로 변환 (더 효율적)
            Map<UUID, String> tagMap = tagRepository.findAllById(List.of(tagId1, tagId2)).stream()
                    .collect(Collectors.toMap(Tag::getId, Tag::getName));

            String tagName1 = tagMap.get(tagId1);
            String tagName2 = tagMap.get(tagId2);

            if (tagName1 == null || tagName2 == null) return null;

            List<Routine> routines = routineRepository.findRoutinesByTagPair(tagId1, tagId2, PageRequest.of(0, limit));
            List<RoutineListResponse> routineList = routines.stream().map(this::toRoutineListResponse).toList();

            return new TagPairSection(tagName1, tagName2, routineList);
        } catch (IllegalArgumentException e) {
            // log.warn("잘못된 UUID 형식: {} 또는 {}", pair.getTag1(), pair.getTag2());
            return null;
        }
    }

    private RoutineListResponse toRoutineListResponse(Routine routine) {
        return RoutineListResponse.fromRoutine(
                routine,
                s3Service.getImageUrl(routine.getImageUrl()),
                routine.getRoutineTags() // 미리 가져온 태그 정보를 직접 사용
        );
    }

}
