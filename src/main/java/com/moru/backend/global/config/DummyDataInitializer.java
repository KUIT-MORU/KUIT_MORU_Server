package com.moru.backend.global.config;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.user.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("dev") // 개발 환경에서만 더미 데이터 생성하도록
@RequiredArgsConstructor
@Transactional
public class DummyDataInitializer implements CommandLineRunner {
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;


    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재하므로 생성 건너뛰기");
            return;
        }
        log.info("===DataFaker을 사용해서 대규모 더미 데이터 생성 시작===");

        // 수동 생성 : 태그, 앱와 같은 고정적인 데이터
        List<Tag> allTags = createManualTags();
        log.info("[1/5] {}개의 태그를 생성함", allTags.size());

        List<App> allApps = createManualApps();
        log.info("[2/5] {}개의 앱을 생성함", allApps.size());

    }

    private List<Tag> createManualTags() {
        Set<String> tagNames = new HashSet<>(Arrays.asList(
                // rou_tag
                "가족", "걷기", "계획", "공강", "공부", "귀가후", "글쓰기", "기록", "기분나쁠때", "기분좋을때",
                "다이어트", "달리기", "도서관", "독서", "드라이브", "등교길", "등교전", "등산", "맑은날", "면접",
                "명상", "모닝루틴", "미팅", "반려동물", "발표", "방정리", "방학", "버스안", "복습", "브이로그",
                "블로그", "비온날", "사진", "산책", "세탁", "쇼핑", "수능", "스케줄", "스터디", "스트레칭",
                "습관", "시험", "식단", "아침", "악기", "암기", "야근중", "업무", "여행", "연습",
                "영어", "예습", "외출전", "외출중", "요가", "요리", "운동", "운전", "음악", "일기",
                "일본어", "일어나서", "일정관리", "자격증", "자기전", "자료조사", "자유시간", "자전거", "작업", "장보기",
                "재테크", "저녁", "점심", "주말밤", "준비", "지하철", "집콕", "청소", "체조", "체중",
                "출근길", "출근전", "취준", "친구", "카페", "토익", "토플", "퇴근길", "퇴근후", "평일밤",
                "프로그래밍", "필사", "하교길", "하교후", "헬스", "혼자", "회의", "회화", "휴식", "휴일",
                // ob_rou_tag1
                "출근길", "지하철", "퇴근길", "모닝루틴", "일어나서", "저녁", "자기전", "휴일", "공강",
                // ob_rou_tag2
                "독서", "과제", "공부", "작업", "다이어트", "수능", "취준", "프로그래밍", "휴식"
        ));

        List<Tag> tagsToCreate = tagNames.stream()
                .map(name -> Tag.builder().id(UUID.randomUUID()).name(name).build())
                .collect(Collectors.toList());
        return tagRepository.saveAll(tagsToCreate);
    }

    private List<App> createManualApps() {
        List<App> apps = Arrays.asList(
                App.builder().id(UUID.randomUUID()).name("카카오톡").packageName("com.kakao.talk").build(),
                App.builder().id(UUID.randomUUID()).name("인스타그램").packageName("com.instagram.android").build(),
                App.builder().id(UUID.randomUUID()).name("유튜브").packageName("com.google.android.youtube").build(),
                App.builder().id(UUID.randomUUID()).name("네이버").packageName("com.naver.app").build()
        );
        return appRepository.saveAll(apps);
    }

}
