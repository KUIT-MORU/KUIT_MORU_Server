package com.moru.backend.global.dummydata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DummyDataPool {

    private static final Random random = new Random();

    // =================================================================
    // 1. 현실적인 데이터 생성을 위한 데이터 풀(Pool) 확장 및 구조화
    // =================================================================

    // 사용자 Bio 생성을 위한 3단 조합 데이터 풀 (대폭 확장)
    private static final Map<String, List<String>> BIO_POOL = new HashMap<>();
    static {
        BIO_POOL.put("greetings", Arrays.asList("안녕하세요!", "반갑습니다.", "함께 성장하고 싶어요.", "꾸준함을 기록합니다.", "데이터로 증명하는 삶.", "오늘도 화이팅!", "잘 부탁드려요.", "기록은 배신하지 않는다.", "매일의 작은 성공을 모아요."));
        BIO_POOL.put("hobbies", Arrays.asList(
                "운동을 사랑하는", "독서와 글쓰기를 즐기는", "코딩에 빠져있는", "여행을 꿈꾸는",
                "음악을 즐겨듣는", "고양이를 키우는", "영화를 좋아하는", "요리하는 것을 즐기는",
                "사진 찍는", "재테크에 관심 많은", "헬스에 진심인", "등산을 즐기는", "자전거 타는",
                "블로그를 운영하는", "브이로그를 만드는", "악기 연주가 취미인", "드라이브를 즐기는"
        ));
        BIO_POOL.put("introductions", Arrays.asList(
                "대학생입니다.", "직장인입니다.", "개발자입니다.", "디자이너입니다.", "기획자입니다.",
                "취준생입니다.", "프리랜서입니다.", "N년차 마케터입니다.", "예비 창업가입니다.",
                "석사 과정 중입니다.", "데이터 분석가입니다.", "프로덕트 매니저(PM)입니다.", "콘텐츠 크리에이터입니다.",
                "연구원입니다.", "선생님입니다.", "공무원입니다.", "자영업자입니다."
        ));
    }

    // 기능별 스텝 데이터 풀 (대폭 확장)
    private static final Map<String, List<String>> STEP_POOL = new HashMap<>();
    static {
        STEP_POOL.put("morning", Arrays.asList("기상", "이불 정리", "물 한잔 마시기", "아침 스트레칭", "오늘 뉴스 확인", "아침 식사", "영양제 챙기기", "오늘 입을 옷 고르기", "모닝커피", "오늘 날씨 확인"));
        STEP_POOL.put("evening", Arrays.asList("샤워", "내일 계획 세우기", "3줄 일기 쓰기", "스킨케어", "명상", "가벼운 독서", "자기전 정리", "하루 지출 정리", "소등 후 팟캐스트 듣기"));
        STEP_POOL.put("workout", Arrays.asList("헬스장 가기", "유산소 30분", "근력 운동 40분", "요가", "필라테스", "달리기", "자전거 타기", "체조", "수영", "클라이밍", "등산하기", "체중 기록하기", "인바디 체크"));
        STEP_POOL.put("study", Arrays.asList("전공 강의 듣기", "코딩 문제 풀기", "오답 노트 정리", "자격증 인강", "자료조사", "필사", "복습", "예습", "스터디 발표 준비", "논문 읽기"));
        STEP_POOL.put("chores", Arrays.asList("방 정리", "청소기 돌리기", "설거지", "분리수거", "장보기", "세탁", "환기", "요리", "화분에 물 주기", "화장실 청소", "옷장 정리"));
        STEP_POOL.put("wellbeing", Arrays.asList("좋아하는 음악 듣기", "따뜻한 차 마시기", "멍때리기", "감사한 일 3가지 생각하기", "반려동물과 놀기", "짧은 산책", "감정정리", "좋아하는 영화 보기", "마스크팩 하기"));
        STEP_POOL.put("commute", Arrays.asList("지하철에서 책 읽기", "버스에서 팟캐스트 듣기", "운전하며 영어 뉴스 듣기", "출근 준비", "퇴근 후 옷 갈아입기", "등교 준비", "하교길 플레이리스트 듣기"));
        STEP_POOL.put("career", Arrays.asList("이력서 업데이트", "채용 공고 확인", "1분 자기소개 연습", "면접 스터디", "업무 이메일 회신", "회의록 작성", "포트폴리오 정리", "업계 동향 파악"));
        STEP_POOL.put("creative", Arrays.asList("블로그 글쓰기", "브이로그 촬영 및 편집", "사진 보정", "악기 연습", "그림 그리기", "아이디어 스케치", "일기 쓰기", "단편 소설 쓰기"));
        STEP_POOL.put("finance", Arrays.asList("가계부 작성", "경제 기사 스크랩", "주식 시장 확인", "절약 챌린지", "재테크 영상 시청", "소비 내역 분석"));
        STEP_POOL.put("generic", Arrays.asList("옷 입기", "알림 확인", "친구에게 연락하기", "커피 내리기", "외출 준비", "점심 식사", "휴식", "간식 시간", "물 2L 마시기"));

        // --- 신규 카테고리 10개 ---
        STEP_POOL.put("planning", Arrays.asList("주간 계획 세우기", "월간 목표 설정", "투두리스트 작성", "연간 계획 검토", "프로젝트 마일스톤 정리", "시간대별 스케줄링"));
        STEP_POOL.put("language", Arrays.asList("영어 단어 100개 암기", "토익 LC/RC 풀기", "영어 뉴스 쉐도잉", "일본어 회화 연습", "토플 모의고사 보기", "외국어 원서 읽기", "언어 교환 모임 참여"));
        STEP_POOL.put("presentation", Arrays.asList("발표 자료 만들기", "발표 스크립트 작성", "1분 자기소개 연습", "예상 면접 질문 답변 준비", "모의 면접 진행", "자신감 있는 목소리 톤 연습"));
        STEP_POOL.put("research", Arrays.asList("논문 검색 및 읽기", "관련 기사 스크랩", "경쟁사 분석", "시장 조사", "레퍼런스 수집", "설문조사 만들기"));
        STEP_POOL.put("social", Arrays.asList("친구와 약속 잡기", "부모님께 안부 전화", "스터디 모임 참석", "동호회 활동", "네트워킹 이벤트 참여", "경조사 챙기기"));
        STEP_POOL.put("beauty", Arrays.asList("마스크팩 하기", "네일 케어", "미용실 방문", "화장품 정리", "아침 메이크업", "헤어 스타일링", "퍼스널 컬러 진단 알아보기"));
        STEP_POOL.put("photography", Arrays.asList("사진 촬영 출사", "사진 보정하기", "영상 편집하기", "장비 점검 및 청소", "사진 인화", "촬영 컨셉 구상"));
        STEP_POOL.put("driving", Arrays.asList("세차하기", "주유하기", "드라이브 코스 짜기", "안전 운전 연습", "차량 정비 예약", "주차 연습"));
        STEP_POOL.put("cafe_tour", Arrays.asList("단골 카페 방문", "새로운 카페 탐방", "카페에서 작업하기", "디저트 맛보기", "원두 구매", "시그니처 메뉴 마셔보기"));
        STEP_POOL.put("reflection", Arrays.asList("감사 일기 쓰기", "오늘의 성공 3가지 적기", "실수 노트 작성", "주간 회고 (KPT)", "월간 회고", "명상하며 생각 정리"));
    }

    // 루틴 생성을 위한 '레시피' 클래스
    @Getter
    @AllArgsConstructor
    public static class RoutineRecipe {
        private final String theme;
        private final List<String> titles;
        private final List<String> contentTemplates;
        private final List<String> coreStepCategories;
        private final List<String> secondaryStepCategories;
    }

    // '레시피' 데이터 풀 (100개로 대폭 확장)
    private static final List<RoutineRecipe> RECIPE_POOL = new ArrayList<>();
    static {
        // 학생/취준생 페르소나 (10)
        RECIPE_POOL.add(new RoutineRecipe("시험공부", Arrays.asList("기말고사 루틴", "밤샘 공부 타임"), Arrays.asList("오늘 밤은 공부와 함께. #%s", "학점은 배신하지 않는다. #%s"), Arrays.asList("study"), Arrays.asList("wellbeing", "generic")));
        RECIPE_POOL.add(new RoutineRecipe("취준", Arrays.asList("취뽀 성공 루틴", "자소서 쓰는 날"), Arrays.asList("합격하는 그날까지! #%s", "미래의 나를 위한 오늘의 투자, #%s"), Arrays.asList("career", "study"), Arrays.asList("wellbeing")));
        RECIPE_POOL.add(new RoutineRecipe("토익", Arrays.asList("900점 목표", "LC/RC 풀기"), Arrays.asList("목표 점수까지 빡공! #%s", "오늘의 #%s 점수 기록"), Arrays.asList("study"), Arrays.asList("commute")));
        RECIPE_POOL.add(new RoutineRecipe("자격증", Arrays.asList("컴활 1급 도전", "정처기 필기"), Arrays.asList("자격증 취득으로 스펙업! #%s", "오늘의 #%s 진도"), Arrays.asList("study"), Arrays.asList("evening")));
        RECIPE_POOL.add(new RoutineRecipe("프로그래밍", Arrays.asList("알고리즘 풀이", "개인 프로젝트"), Arrays.asList("1일 1커밋 #%s", "에러 없는 코드를 위해. #%s"), Arrays.asList("study", "creative"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("등교", Arrays.asList("등교 준비", "공강 시간 활용"), Arrays.asList("오늘도 지각 없이! #%s", "알찬 하루의 시작 #%s"), Arrays.asList("morning", "commute"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("하교", Arrays.asList("하교 후 리프레시", "과제 전 웜업"), Arrays.asList("오늘도 수고했어. #%s", "집에 와서 하는 일 #%s"), Arrays.asList("commute", "evening"), Arrays.asList("wellbeing")));
        RECIPE_POOL.add(new RoutineRecipe("발표준비", Arrays.asList("발표 자료 제작", "발표 연습"), Arrays.asList("완벽한 발표를 위해. #%s", "떨지말고 잘하자! #%s"), Arrays.asList("study", "career"), Arrays.asList("wellbeing")));
        RECIPE_POOL.add(new RoutineRecipe("방학", Arrays.asList("알찬 방학 계획", "방학 중 자기계발"), Arrays.asList("방학을 낭비하지 않는 법 #%s", "두 달 뒤, 달라진 나를 위해. #%s"), Arrays.asList("study", "workout", "wellbeing"), Arrays.asList("chores")));
        RECIPE_POOL.add(new RoutineRecipe("도서관", Arrays.asList("도서관 출첵", "도서관 공부법"), Arrays.asList("오늘도 도서관에서 #%s", "열람실 나의 자리 #%s"), Arrays.asList("study"), Arrays.asList("commute", "generic")));

        // 직장인 페르소나 (10)
        RECIPE_POOL.add(new RoutineRecipe("출근", Arrays.asList("상쾌한 출근길", "지옥철 생존기"), Arrays.asList("프로 직장인의 아침 #%s", "오늘도 칼퇴를 위해! #%s"), Arrays.asList("morning", "commute"), Arrays.asList("career")));
        RECIPE_POOL.add(new RoutineRecipe("퇴근", Arrays.asList("퇴근 후 워라밸", "수고했어 오늘도"), Arrays.asList("지친 몸과 마음을 위한 #%s", "퇴근 후 나만의 시간 #%s"), Arrays.asList("evening", "wellbeing"), Arrays.asList("chores")));
        RECIPE_POOL.add(new RoutineRecipe("업무", Arrays.asList("야근 방지 집중", "회의 전 준비"), Arrays.asList("일잘러의 비밀 #%s", "오늘 업무 클리어! #%s"), Arrays.asList("career"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("자기계발", Arrays.asList("퇴근 후 1시간", "사이드 프로젝트"), Arrays.asList("회사 밖에서 성장하기 #%s", "나의 가치를 높이는 #%s"), Arrays.asList("study", "creative"), Arrays.asList("evening")));
        RECIPE_POOL.add(new RoutineRecipe("재테크", Arrays.asList("경제적 자유", "오늘의 짠테크"), Arrays.asList("티끌 모아 태산! 나의 #%s 챌린지", "부자되는 습관, #%s"), Arrays.asList("finance", "study"), Arrays.asList("commute")));
        RECIPE_POOL.add(new RoutineRecipe("영어공부", Arrays.asList("비즈니스 영어", "출근길 영어"), Arrays.asList("글로벌 인재가 되기 위한 #%s", "해외 출장 대비 #%s"), Arrays.asList("study", "career"), Arrays.asList("commute")));
        RECIPE_POOL.add(new RoutineRecipe("면접", Arrays.asList("이직 면접 준비", "경력 기술서 갱신"), Arrays.asList("더 좋은 조건으로 점프! #%s", "나의 강점을 어필하기 #%s"), Arrays.asList("career"), Arrays.asList("wellbeing")));
        RECIPE_POOL.add(new RoutineRecipe("야근", Arrays.asList("야근 중", "야근 후"), Arrays.asList("야근해도 괜찮아. #%s", "오늘의 야근 기록 #%s"), Arrays.asList("career", "evening"), Arrays.asList("wellbeing")));
        RECIPE_POOL.add(new RoutineRecipe("회의", Arrays.asList("회의 전 정리", "회의록 작성"), Arrays.asList("생산적인 회의를 위한 #%s", "오늘의 회의록 #%s"), Arrays.asList("career"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("주말출근", Arrays.asList("주말 출근", "주말 업무"), Arrays.asList("주말에도 열일! #%s", "빨리 끝내고 쉬자! #%s"), Arrays.asList("career", "commute"), Arrays.asList("wellbeing")));

        // 건강/웰빙 페르소나 (10)
        RECIPE_POOL.add(new RoutineRecipe("헬스", Arrays.asList("3대 500", "상체 운동"), Arrays.asList("오늘도 득근! #%s", "쇠질은 배신하지 않는다. #%s"), Arrays.asList("workout"), Arrays.asList("wellbeing", "evening")));
        RECIPE_POOL.add(new RoutineRecipe("다이어트", Arrays.asList("건강한 식단 기록", "체중 감량"), Arrays.asList("가벼워지는 즐거움, #%s", "오늘의 #%s 식단일기"), Arrays.asList("workout", "chores"), Arrays.asList("wellbeing", "morning")));
        RECIPE_POOL.add(new RoutineRecipe("요가/명상", Arrays.asList("아침 요가", "잠들기 전 명상"), Arrays.asList("몸과 마음의 평화 #%s", "나에게 집중하는 시간 #%s"), Arrays.asList("workout", "wellbeing"), Arrays.asList("morning", "evening")));
        RECIPE_POOL.add(new RoutineRecipe("달리기", Arrays.asList("저녁 공원 러닝", "상쾌한 아침 조깅"), Arrays.asList("오늘의 러닝 기록 #%s", "생각을 비우는 시간 #%s"), Arrays.asList("workout"), Arrays.asList("morning", "evening")));
        RECIPE_POOL.add(new RoutineRecipe("산책", Arrays.asList("점심 식사 후 산책", "저녁 산책"), Arrays.asList("오늘의 걸음 수 #%s", "소소한 행복 #%s"), Arrays.asList("wellbeing"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("식단", Arrays.asList("클린식단", "샐러드 준비"), Arrays.asList("건강한 한 끼 #%s", "내 몸을 위한 #%s"), Arrays.asList("chores"), Arrays.asList("morning")));
        RECIPE_POOL.add(new RoutineRecipe("마음챙김", Arrays.asList("위로 시간", "감정 일기 쓰기"), Arrays.asList("오늘 하루도 수고했어. #%s", "내 마음을 돌보는 시간, #%s"), Arrays.asList("wellbeing", "evening"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("기분전환", Arrays.asList("나를 위한 처방전", "기분 좋을 때!"), Arrays.asList("이 기분 그대로! #%s", "나를 위한 작은 선물, #%s"), Arrays.asList("wellbeing", "creative"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("수면", Arrays.asList("수면 루틴", "일찍 자기"), Arrays.asList("최고의 회복은 수면 #%s", "내일을 위한 준비 #%s"), Arrays.asList("evening", "wellbeing"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("스트레칭", Arrays.asList("스트레칭", "거북목 탈출"), Arrays.asList("시원하다! #%s", "오늘의 #%s 완료"), Arrays.asList("workout", "wellbeing"), Arrays.asList("morning", "evening")));

        // 라이프스타일/취미 페르소나 (10)
        RECIPE_POOL.add(new RoutineRecipe("요리", Arrays.asList("오늘 저녁", "주말 베이킹"), Arrays.asList("요리는 즐거워 #%s", "내가 만든 건강한 식사 #%s"), Arrays.asList("chores"), Arrays.asList("evening", "generic")));
        RECIPE_POOL.add(new RoutineRecipe("청소", Arrays.asList("주말 대청소", "매일 15분 정리"), Arrays.asList("깨끗한 공간, 맑은 정신 #%s", "오늘의 #%s 구역"), Arrays.asList("chores"), Arrays.asList("evening", "morning")));
        RECIPE_POOL.add(new RoutineRecipe("독서", Arrays.asList("책 읽기", "필사"), Arrays.asList("책 속에 길이 있다 #%s", "오늘의 한 문장 #%s"), Arrays.asList("study", "wellbeing"), Arrays.asList("commute", "evening")));
        RECIPE_POOL.add(new RoutineRecipe("음악", Arrays.asList("기타 연습 30분", "오늘 플레이리스트"), Arrays.asList("음악은 나의 힘 #%s", "손가락이 굳지 않게 #%s"), Arrays.asList("creative", "wellbeing"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("블로그", Arrays.asList("1일 1포스팅 도전", "콘텐츠 아이디어"), Arrays.asList("나의 생각을 기록하다 #%s", "오늘의 #%s 포스팅 완료"), Arrays.asList("creative", "study"), Arrays.asList("evening")));
        RECIPE_POOL.add(new RoutineRecipe("반려동물", Arrays.asList("댕댕이 산책", "고양이 화장실"), Arrays.asList("우리집 막내를 위해 #%s", "사랑하는 나의 #%s"), Arrays.asList("chores", "wellbeing"), Arrays.asList("morning", "evening")));
        RECIPE_POOL.add(new RoutineRecipe("여행", Arrays.asList("여행 계획", "출국 D-1트"), Arrays.asList("떠나자! #%s", "완벽한 여행을 위한 준비 과정 #%s"), Arrays.asList("chores", "finance"), Arrays.asList("wellbeing")));
        RECIPE_POOL.add(new RoutineRecipe("친구", Arrays.asList("친구와 약속", "친구와 통화"), Arrays.asList("만나면 즐거운 #%s", "소중한 인연 #%s"), Arrays.asList("generic"), Arrays.asList("wellbeing")));
        RECIPE_POOL.add(new RoutineRecipe("쇼핑", Arrays.asList("온라인 쇼핑", "장보러 가기 전"), Arrays.asList("현명한 소비를 위해 #%s", "나를 위한 선물 #%s"), Arrays.asList("finance", "chores"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("휴일", Arrays.asList("주말 리프레시", "휴일 아침"), Arrays.asList("온전히 나에게 집중하는 #%s", "이번 주말도 알차게! #%s"), Arrays.asList("chores", "wellbeing"), Arrays.asList("workout", "generic")));

        // 날씨/기분 페르소나 (10)
        RECIPE_POOL.add(new RoutineRecipe("맑은날", Arrays.asList("산책", "대청소"), Arrays.asList("날씨가 다했다! #%s", "광합성 제대로 하는 중 #%s"), Arrays.asList("wellbeing", "chores"), Arrays.asList("workout")));
        RECIPE_POOL.add(new RoutineRecipe("비온날", Arrays.asList("감성 독서", "집콕"), Arrays.asList("빗소리 들으며 #%s", "센치해지는 오늘 #%s"), Arrays.asList("wellbeing", "creative"), Arrays.asList("chores")));
        RECIPE_POOL.add(new RoutineRecipe("기분좋을때", Arrays.asList("에너지 발산", "드라이브"), Arrays.asList("이 기분 그대로! #%s", "행복은 가까이에 #%s"), Arrays.asList("wellbeing", "creative"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("기분나쁠때", Arrays.asList("우울한 날", "스트레스 해소"), Arrays.asList("이 또한 지나가리라 #%s", "기분 전환을 위한 #%s"), Arrays.asList("wellbeing", "workout"), Arrays.asList("chores")));
        RECIPE_POOL.add(new RoutineRecipe("혼자", Arrays.asList("혼자만의 시간", "나를 위한 요리"), Arrays.asList("누구의 방해도 받지 않는 #%s", "오롯이 나에게 집중 #%s"), Arrays.asList("wellbeing", "creative", "chores"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("가족", Arrays.asList("부모님 안부 전화", "가족 저녁 식사"), Arrays.asList("사랑하는 #%s", "가족은 나의 힘 #%s"), Arrays.asList("evening"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("자유시간", Arrays.asList("자유시간 활용법", "하고 싶었던 것"), Arrays.asList("뜻밖의 여유 #%s", "이 시간을 즐기자 #%s"), Arrays.asList("wellbeing", "creative"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("집콕", Arrays.asList("집콕의 날", "드라마 정주행"), Arrays.asList("집이 최고야 #%s", "오늘의 방구석 1열 #%s"), Arrays.asList("wellbeing", "chores"), Arrays.asList("creative")));
        RECIPE_POOL.add(new RoutineRecipe("외출", Arrays.asList("외출 전 점검", "카페 투어"), Arrays.asList("오늘의 외출 기록 #%s", "오랜만의 바깥 공기 #%s"), Arrays.asList("commute", "generic"), Arrays.asList("wellbeing")));
        RECIPE_POOL.add(new RoutineRecipe("평일밤", Arrays.asList("평일밤 루틴", "리프레시"), Arrays.asList("평일 저녁을 알차게 #%s", "오늘 하루 마무리 #%s"), Arrays.asList("evening", "wellbeing"), Arrays.asList("chores", "study")));
        // 생산성/계획 페르소나 (10)
        RECIPE_POOL.add(new RoutineRecipe("갓생 살기", Arrays.asList("갓생러의 하루", "계획적인 삶"), Arrays.asList("오늘도 갓생 성공! #%s", "계획이 나를 만든다 #%s"), Arrays.asList("planning", "morning", "evening"), Arrays.asList("study", "workout", "reflection")));
        RECIPE_POOL.add(new RoutineRecipe("주간 계획", Arrays.asList("일주일 계획", "주간 목표 달성"), Arrays.asList("이번 주도 화이팅! #%s", "계획대로 착착 #%s"), Arrays.asList("planning"), Arrays.asList("reflection", "career")));
        RECIPE_POOL.add(new RoutineRecipe("시간 관리", Arrays.asList("뽀모도로 기법", "시간 가계부"), Arrays.asList("시간을 지배하는 자 #%s", "낭비 없는 하루 #%s"), Arrays.asList("planning", "study"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("프로젝트 관리", Arrays.asList("프로젝트 진행", "프로젝트 마일스톤"), Arrays.asList("차근차근 빌드업 #%s", "오늘의 #%s 개발일지"), Arrays.asList("planning", "career", "creative"), Arrays.asList("study")));
        RECIPE_POOL.add(new RoutineRecipe("목표 설정", Arrays.asList("올해 목표 설정", "목표 점검"), Arrays.asList("목표를 향해 전진 #%s", "꿈은 이루어진다 #%s"), Arrays.asList("planning", "reflection"), Arrays.asList("finance", "career")));
        RECIPE_POOL.add(new RoutineRecipe("습관 형성", Arrays.asList("작심삼일 탈출", "좋은 습관"), Arrays.asList("습관이 된 #%s", "21일의 기적 #%s"), Arrays.asList("planning", "wellbeing"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("디지털 디톡스", Arrays.asList("스마트폰 없이", "마음이 편안해지는"), Arrays.asList("잠시 로그아웃 합니다. #%s", "디지털 디톡스로 얻는 평화 #%s"), Arrays.asList("wellbeing", "evening"), Arrays.asList("chores", "creative")));
        RECIPE_POOL.add(new RoutineRecipe("미라클 모닝", Arrays.asList("새벽 5시 기상", "아침 활용"), Arrays.asList("아침을 지배하는 자, 하루를 지배한다. #%s", "고요한 새벽의 #%s"), Arrays.asList("morning", "planning", "reflection"), Arrays.asList("workout", "study")));
        RECIPE_POOL.add(new RoutineRecipe("주간 회고", Arrays.asList("KPT 주간 회고", "나를 위한 시간"), Arrays.asList("이번 주를 돌아보며 #%s", "다음 주를 준비하는 #%s"), Arrays.asList("reflection", "planning"), Arrays.asList("career")));
        RECIPE_POOL.add(new RoutineRecipe("일정 관리", Arrays.asList("구글 캘린더 정리", "노션 스케줄 관리"), Arrays.asList("정리된 삶 #%s", "오늘의 스케줄 #%s"), Arrays.asList("planning"), Arrays.asList("generic", "career")));

        // 전문성/커리어 페르소나 (10)
        RECIPE_POOL.add(new RoutineRecipe("이직 준비", Arrays.asList("이직 성공 플랜", "회사 리서치"), Arrays.asList("커리어 점프업! #%s", "나의 가치를 높이는 시간 #%s"), Arrays.asList("career", "presentation", "research"), Arrays.asList("social")));
        RECIPE_POOL.add(new RoutineRecipe("네트워킹", Arrays.asList("링크드인", "커피챗"), Arrays.asList("인맥도 자산이다 #%s", "새로운 기회를 찾아서 #%s"), Arrays.asList("social", "career"), Arrays.asList("generic")));
        RECIPE_POOL.add(new RoutineRecipe("외국어 회화", Arrays.asList("영어 회화 스터디", "쉐도잉 연습"), Arrays.asList("자신감있게 말하기 #%s", "오늘의 #%s 회화"), Arrays.asList("language", "presentation"), Arrays.asList("social")));
    }

    public String getRandomBio() {
        return BIO_POOL.get("greetings").get(random.nextInt(BIO_POOL.get("greetings").size())) + " " +
                BIO_POOL.get("hobbies").get(random.nextInt(BIO_POOL.get("hobbies").size())) + " " +
                BIO_POOL.get("introductions").get(random.nextInt(BIO_POOL.get("introductions").size()));
    }

    public RoutineRecipe getRandomRecipe() {
        return RECIPE_POOL.get(random.nextInt(RECIPE_POOL.size()));
    }

    public Set<String> getRandomStepsFromCategories(List<String> categories, int count) {
        Set<String> steps = new HashSet<>();
        if (categories == null || categories.isEmpty() || count <= 0) {
            return steps;
        }
        while (steps.size() < count) {
            String randomCategory = categories.get(random.nextInt(categories.size()));
            List<String> stepPool = STEP_POOL.get(randomCategory);
            if (stepPool != null && !stepPool.isEmpty()) {
                steps.add(stepPool.get(random.nextInt(stepPool.size())));
            }
        }
        return steps;
    }
}