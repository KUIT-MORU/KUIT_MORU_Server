package com.moru.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 공통 예외
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),

    // 인증 관련 예외
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    USER_NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다."),

    // 유저 관련 예외
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "중복된 닉네임입니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "잘못된 닉네임입니다."),
    USER_DEACTIVATED(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다. 로그인할 수 없습니다." ),
    USER_NOT_MATCH(HttpStatus.CONFLICT, "루틴을 생성한 유저와 매칭되지 않는 유저입니다."),

    // 태그 관련 예외
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 태그입니다."),
    TAG_ALREAEDY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 태그입니다."),
    ALREADY_CONNECTED_TAG(HttpStatus.CONFLICT, "이미 연결된 태그입니다."),

    // 소셜 관련 예외
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 좋아요입니다."),
    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 좋아요한 루틴입니다."),
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 스크랩입니다."),
    SCRAP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 스크랩한 루틴입니다."),
    SCRAP_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신의 루틴을 스크랩할 수 없습니다."),
    IMPORT_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신의 루틴을 복사해 가져올 수 없습니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 팔로우입니다."),
    FOLLOW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 팔로우입니다."),
    FOLLOW_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),

    // 루틴 관련 예외
    ROUTINE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 루틴입니다."),
    TAG_OVERLOADED(HttpStatus.BAD_REQUEST, "루틴에는 최대 3개의 태그만 연결할 수 있습니다."),
    ROUTINE_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "루틴에 연결된 태그를 찾을 수 없습니다."),

    APP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 앱입니다."),
    ALREADY_CONNECTED_APP(HttpStatus.CONFLICT, "이미 연결된 앱입니다."),
    ROUTINE_APP_NOT_FOUND(HttpStatus.NOT_FOUND, "루틴에 연결된 앱을 찾을 수 없습니다."),

    INVALID_REPEAT_TYPE(HttpStatus.BAD_REQUEST, "반복되는 요일을 찾을 수 없습니다.."),
    ALREADY_EXISTS_SCHEDULE(HttpStatus.CONFLICT, "이미 해당 시간대에 존재한느 루틴이 있습니다."),

    // 루틴 로그 관련 예외
    ROUTINE_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 루틴 로그입니다."),

    // 스텝 관련 예외
    STEP_OVERLOADED(HttpStatus.BAD_REQUEST, "루틴 당 스텝의 최대 개수는 6개입니다"),
    INVALID_STEP_ORDER(HttpStatus.BAD_REQUEST, "유효하지 않은 스텝 순서입니다."),
    ROUTINE_STEP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 아이디의 루틴 스텝이 존재하지 않습니다"),

    // 검색 관련 예외
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "검색 기록을 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
