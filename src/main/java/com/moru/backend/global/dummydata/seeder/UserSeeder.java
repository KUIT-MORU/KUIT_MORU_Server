package com.moru.backend.global.dummydata.seeder;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.Gender;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.domain.UserRole;
import com.moru.backend.global.dummydata.DummyDataPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSeeder {
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final DummyDataPool dummyDataPool;

    private final Faker faker = new Faker(new Locale("ko"));
    private final Random random = new Random();

    private static final String COMMON_PASSWORD_HASH = "$2a$12$/OXNM8oYy5chh/iOUA3j3.XjIEYi9Zbg/kiVT3.D/.zP2cev/5EDq"; // 1234abcde!@
    private static final int BATCH_SIZE = 200;

    @Transactional(readOnly = true)
    public boolean isDataPresent() {
        return userRepository.count() > 0;
    }

    @Transactional
    public List<Tag> createManualTags() {
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
                .map(name -> Tag.builder().name(name).build()) // <- ID 생성 코드 삭제
                .collect(Collectors.toList());
        return tagRepository.saveAll(tagsToCreate);
    }

    @Transactional
    public List<App> createManualApps() {
        List<App> apps = Arrays.asList(
                App.builder().name("카카오톡").packageName("com.kakao.talk").build(),
                App.builder().name("인스타그램").packageName("com.instagram.android").build(),
                App.builder().name("유튜브").packageName("com.google.android.youtube").build(),
                App.builder().name("네이버").packageName("com.naver.app").build()
        );
        return appRepository.saveAll(apps);
    }

    @Transactional
    public List<User> createBulkUsers(int count) {
        List<User> allGeneratedUsers = new ArrayList<>();
        List<User> userBatch = new ArrayList<>();

        // 1. 테스트용 고정 사용자 추가 (data.sql 내용 반영)
        User testUser = User.builder()
                .email("test@example.com")
                .password("$2a$10$xUBrBGPuFIPdnDU2LCRLOeb3ML.vcGEen7NrughMZcQAs/i4cbxsy")
                .nickname("테스트유저")
                .gender(Gender.MALE)
                .birthday(LocalDate.parse("2000-01-01"))
                .bio("테스트 계정입니다.")
                .profileImageUrl("https://example.com/profile0.jpg")
                .role(UserRole.ADMIN)
                // status, createdAt, updatedAt은 자동 처리되므로 설정 불필요
                .build();
        userBatch.add(testUser);

        for (int i = 1; i < count; i++) {
            // 예측 가능한 고유 이메일과 닉네임 생성
            String email = "user" + i + "@moru.com";
            String nickname = "모루유저" + i;

            LocalDate birthday = faker.date().birthday(18, 65).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            userBatch.add(User.builder()
                    .email(email)
                    .password(COMMON_PASSWORD_HASH)
                    .nickname(nickname)
                    .gender(random.nextBoolean() ? Gender.MALE : Gender.FEMALE)
                    .birthday(birthday)
                    .bio(dummyDataPool.getRandomBio())
                    // 80% 확률로 사용자 프로필 이미지(DiceBear) 추가
                    .profileImageUrl(random.nextInt(10) < 8 ? dummyDataPool.getRandomUserProfileImage(nickname) : null)
                    .role(UserRole.USER)
                    // status, createdAt, updatedAt은 자동 처리되므로 설정 불필요
                    .build());
            if (userBatch.size() >= BATCH_SIZE) {
                allGeneratedUsers.addAll(userRepository.saveAll(userBatch));
                userBatch.clear();
                log.info("{}명의 사용자 중간 저장 완료...", allGeneratedUsers.size());
            }
        }
        if (!userBatch.isEmpty()) {
            allGeneratedUsers.addAll(userRepository.saveAll(userBatch));
        }
        return allGeneratedUsers;
    }

}
