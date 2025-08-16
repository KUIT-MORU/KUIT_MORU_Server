package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.routine.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.dao.TagPairCount;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.dto.response.RecommendFeedResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.routine.dto.response.TagPairSection;
import com.moru.backend.domain.user.dao.UserFavoriteTagRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoutineRecommendService {
    private final RoutineRepository routineRepository;
    private final RoutineTagRepository routineTagRepository;
    private final RoutineQueryService routineQueryService;
    private final RoutineLogRepository routineLogRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;
    private final TagRepository tagRepository;
    private final S3Service s3Service;

    @Value("${moru.routine.recommend.hot-score.view-weight}")
    private double viewWeight;

    @Value("${moru.routine.recommend.hot-score.like-weight}")
    private double likeWeight;

    // TagPairSection의 중간 결과를 담기 위한 private record
    private record TagPairSectionResult(String tag1, String tag2, List<Routine> routines) {}

    public RecommendFeedResponse getRecommendFeed(User user) {
        // 1. 각 섹션별로 추천될 Routine 엔티티 목록을 먼저 가져온다
        List<Routine> hotRoutines = findHotRoutines(10);
        List<Routine> personalRoutines = findPersonalRoutines(user, 10);
        TagPairSectionResult tagPairResult1 = findTopTagPairSection(10);
        TagPairSectionResult tagPairResult2 = findInterestTagPairSection(user, 10);

        // 2. 모든 추천 루틴을 한 곳에 모으고, 루틴의 소유자 ID 목록을 추출한다
        Set<Routine> allRoutines = new HashSet<>();
        allRoutines.addAll(hotRoutines);
        allRoutines.addAll(personalRoutines);
        if (tagPairResult1 != null) allRoutines.addAll(tagPairResult1.routines());
        if (tagPairResult2 != null) allRoutines.addAll(tagPairResult2.routines());

        if (allRoutines.isEmpty()) {
            return new RecommendFeedResponse(Collections.emptyList(), Collections.emptyList(), null, null);
        }

        List<UUID> ownerIds = allRoutines.stream()
                .map(routine -> routine.getUser().getId())
                .distinct()
                .toList();

        // 3. 소유자 목록을 사용해, 현재 실행 중인 모든 로그를 DB에서 단 한 번만 조회
        List<RoutineLog> activeLogs = routineLogRepository.findActiveLogsForUsers(ownerIds);

        // 4. "소유주에 의해 실행 중인" 루틴의 ID만 필터링하여 Set으로
        Map<UUID, UUID> routineToOwnerMap = allRoutines.stream()
                .collect(Collectors.toMap(Routine::getId, r -> r.getUser().getId()));

        Set<UUID> runningByOwnerRoutineIds = activeLogs.stream()
                .filter(log -> {
                    UUID routineId = log.getRoutineSnapshot().getOriginalRoutineId();
                    UUID runnerId = log.getUser().getId();
                    // 이 로그를 실행한 사람(runnerId)이 이 루틴의 소유주(owner)인지 확인
                    return runnerId.equals(routineToOwnerMap.get(routineId));
                })
                .map(log -> log.getRoutineSnapshot().getOriginalRoutineId())
                .collect(Collectors.toSet());

        // 5. 최종 응답 DTO를 만들면서, isRunning 값을 설정
        List<RoutineListResponse> hotRoutineDTOs = hotRoutines.stream()
                .map(r -> toRoutineListResponse(r, runningByOwnerRoutineIds.contains(r.getId())))
                .toList();

        List<RoutineListResponse> personalRoutineDTOs = personalRoutines.stream()
                .map(r -> toRoutineListResponse(r, runningByOwnerRoutineIds.contains(r.getId())))
                .toList();

        TagPairSection section1 = buildTagPairSectionDto(tagPairResult1, runningByOwnerRoutineIds);
        TagPairSection section2 = buildTagPairSectionDto(tagPairResult2, runningByOwnerRoutineIds);

        return new RecommendFeedResponse(hotRoutineDTOs, personalRoutineDTOs, section1, section2);
    }

    /**
     * 지금 가장 핫한 루틴 (엔티티 반환)
     */
    private List<Routine> findHotRoutines(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<UUID> hotRoutineIds = routineRepository.findHotRoutinesIds(weekAgo, viewWeight, likeWeight, PageRequest.of(0, limit));
        if (hotRoutineIds.isEmpty()) {
            return Collections.emptyList();
        }
        return routineQueryService.findAndSortRoutinesWithDetails(hotRoutineIds);
    }

    /**
     * 개인화 추천 (엔티티 반환)
     */
    private List<Routine> findPersonalRoutines(User user, int limit) {
        Set<UUID> recommendedRoutineIds = new HashSet<>();
        List<Routine> result = new ArrayList<>();

        List<String> primaryTags = Stream.concat(
                userFavoriteTagRepository.findFavoriteTagNamesByUserId(user.getId()).stream(),
                routineTagRepository.findTagNamesByUserRoutines(user).stream()
        ).toList();
        List<String> secondaryTags = routineTagRepository.findTagNamesByUserScrappedRoutines(user);

        fillRoutineEntitiesFromTags(primaryTags, recommendedRoutineIds, result, limit);

        if (result.size() < limit) {
            fillRoutineEntitiesFromTags(secondaryTags, recommendedRoutineIds, result, limit);
        }

        if (result.size() < limit) {
            fillWithHotRoutineEntities(recommendedRoutineIds, result, limit);
        }

        return result;
    }

    /**
     * 태그 조합 추천 (중간 결과 반환)
     */
    private TagPairSectionResult findTopTagPairSection(int limit) {
        List<TagPairCount> topPairs = routineTagRepository.findTopTagPairs();
        for (TagPairCount pair : topPairs) {
            TagPairSectionResult section = buildTagPairSectionResult(pair, limit);
            if (section != null && !section.routines().isEmpty()) {
                return section;
            }
        }
        return null;
    }

    private TagPairSectionResult findInterestTagPairSection(User user, int limit) {
        List<UUID> interestTagIds = userFavoriteTagRepository.findAllByUserId(user.getId()).stream()
                .map(uft -> uft.getTag().getId())
                .toList();

        if (interestTagIds.isEmpty()) {
            return null;
        }

        List<TagPairCount> relevantPairs = routineTagRepository.findTopTagPairsForInterests(interestTagIds);

        for (TagPairCount pair : relevantPairs) {
            TagPairSectionResult section = buildTagPairSectionResult(pair, limit);
            if (section != null && !section.routines().isEmpty()) {
                return section;
            }
        }
        return null;
    }


    // ========================= 유틸/헬퍼 ========================= //
    private void fillRoutineEntitiesFromTags(List<String> tagNames, Set<UUID> existingIds, List<Routine> result, int limit) {
        if (tagNames.isEmpty() || result.size() >= limit) {
            return;
        }
        List<String> sortedTags = sortTagsByFrequency(tagNames);
        Page<UUID> routineIdPage = routineRepository.findRoutineIdsByTagsOrderByTagCount(sortedTags, PageRequest.of(0, limit));
        List<UUID> routineIds = routineIdPage.getContent();
        if (routineIds.isEmpty()) {
            return;
        }
        List<Routine> sortedRoutines = routineQueryService.findAndSortRoutinesWithDetails(routineIds);
        for (Routine routine : sortedRoutines) {
            if (result.size() >= limit) break;
            if (existingIds.add(routine.getId())) {
                result.add(routine);
            }
        }
    }

    private List<String> sortTagsByFrequency(List<String> tagNames) {
        return tagNames.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    private void fillWithHotRoutineEntities(Set<UUID> existingIds, List<Routine> results, int limit) {
        if (results.size() >= limit) {
            return;
        }
        List<Routine> hotRoutines = findHotRoutines(limit * 2);
        for (Routine routine : hotRoutines) {
            if (results.size() >= limit) break;
            if (existingIds.add(routine.getId())) {
                results.add(routine);
            }
        }
    }

    private TagPairSectionResult buildTagPairSectionResult(TagPairCount pair, int limit) {
        if (pair == null || pair.getTag1() == null || pair.getTag2() == null) {
            return null;
        }
        try {
            UUID tagId1 = UUID.fromString(pair.getTag1());
            UUID tagId2 = UUID.fromString(pair.getTag2());
            Map<UUID, String> tagMap = tagRepository.findAllById(List.of(tagId1, tagId2)).stream()
                    .collect(Collectors.toMap(Tag::getId, Tag::getName));
            String tagName1 = tagMap.get(tagId1);
            String tagName2 = tagMap.get(tagId2);
            if (tagName1 == null || tagName2 == null) return null;

            Page<UUID> routineIdPage = routineRepository.findRoutineIdsByTagPair(tagId1, tagId2, PageRequest.of(0, limit));
            if (routineIdPage.isEmpty()) {
                return new TagPairSectionResult(tagName1, tagName2, Collections.emptyList());
            }
            List<Routine> routines = routineQueryService.findAndSortRoutinesWithDetails(routineIdPage.getContent());
            return new TagPairSectionResult(tagName1, tagName2, routines);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private TagPairSection buildTagPairSectionDto(TagPairSectionResult result, Set<UUID> runningIds) {
        if (result == null) {
            return null;
        }
        List<RoutineListResponse> dtoList = result.routines().stream()
                .map(r -> toRoutineListResponse(r, runningIds.contains(r.getId())))
                .toList();
        return new TagPairSection(result.tag1(), result.tag2(), dtoList);
    }

    private RoutineListResponse toRoutineListResponse(Routine routine, boolean isRunning) {
        List<RoutineTag> tagsToUse = routine.getRoutineTags().stream()
                .findFirst()
                .map(List::of)
                .orElse(Collections.emptyList());

        return RoutineListResponse.fromRoutine(
                routine,
                s3Service.getImageUrl(routine.getImageUrl()),
                tagsToUse,
                isRunning
        );
    }
}