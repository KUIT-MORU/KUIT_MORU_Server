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
// feed_main
public class RoutineRecommendService {
    private final RoutineRepository routineRepository;
    private final RoutineTagRepository routineTagRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;
    private final TagRepository tagRepository;
    private final S3Service s3Service;
    private final RoutineQueryService routineQueryService;

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
        // ID 조회 (변경된 Repository 메서드 호출)
        List<UUID> hotRoutineIds = routineRepository.findHotRoutinesIds(weekAgo, viewWeight, likeWeight, PageRequest.of(0, limit));
        if (hotRoutineIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 상세 정보 조회
        List<Routine> hotRoutines = routineQueryService.findAndSortRoutinesWithDetails(hotRoutineIds);

        return hotRoutines.stream()
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
        // 가장 인기있는 쌍부터 시도, 성공하는 첫 번째 섹션을 반환
        for (TagPairCount pair : topPairs) {
            TagPairSection section = buildTagPairSection(pair, limit);
            // 섹션이 성공적으로 생성되었고, 루틴 목록이 비어있지 않다면 반환
            if (section != null && !section.routines().isEmpty()) {
                return section;
            }
        }
        // 모든 쌍을 시도했지만 유효한 섹션을 만들지 못한 경우
        return null;
    }

    public TagPairSection getInterestTagPairSection(User user, int limit) {
        // 쿼리에 직접 전달하기 위해 UUID 리스트로 조회
        List<UUID> interestTagIds = userFavoriteTagRepository.findAllByUserId(user.getId()).stream()
                .map(uft -> uft.getTag().getId())
                .toList();

        if (interestTagIds.isEmpty()) {
            return null;
        }

        // 사용자의 관심 태그를 직접 사용하여 관련 인기 조합을 조회하는 새 메서드 호출
        List<TagPairCount> relevantPairs = routineTagRepository.findTopTagPairsForInterests(interestTagIds);

        // 필터링된 쌍 중에서 성공하는 첫 번째 섹션을 반환
        for (TagPairCount pair : relevantPairs) {

            TagPairSection section = buildTagPairSection(pair, limit);
            if (section != null && !section.routines().isEmpty()) {
                return section;
            }
        }
        return null;
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

        // 1단계: 정렬된 루틴 ID 목록을 페이지네이션 정보와 함께 조회
        Page<UUID> routineIdPage = routineRepository.findRoutineIdsByTagsOrderByTagCount(sortedTags, PageRequest.of(0, limit));
        List<UUID> routineIds = routineIdPage.getContent();

        if (routineIds.isEmpty()) {
            return;
        }

        List<Routine> sortedRoutines = routineQueryService.findAndSortRoutinesWithDetails(routineIds);

        for (Routine routine : sortedRoutines) {
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

            Page<UUID> routineIdPage = routineRepository.findRoutineIdsByTagPair(tagId1, tagId2, PageRequest.of(0, limit));
            List<UUID> routineIds = routineIdPage.getContent();

            if (routineIds.isEmpty()) {
                return new TagPairSection(tagName1, tagName2, Collections.emptyList());
            }

            List<Routine> sortedRoutines = routineQueryService.findAndSortRoutinesWithDetails(routineIds);

            List<RoutineListResponse> routineList = sortedRoutines.stream()
                    .map(this::toRoutineListResponse)
                    .toList();

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
