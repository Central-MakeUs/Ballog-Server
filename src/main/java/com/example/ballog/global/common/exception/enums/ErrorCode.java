package com.example.ballog.global.common.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // JWT
    NO_REFRESH_TOKEN(400, "JWT001", "refresh 토큰이 존재하지 않습니다."),
    NOT_MATCHING_TOKEN(401, "JWT002", "토큰이 일치하지 않습니다."),
    INVALID_TOKEN(402, "JWT003", "유효하지 않은 토큰입니다."),
    UNAUTHORIZED(404, "AUTH001", "인증 정보가 없습니다."),
    OAUTH_TOKEN_NOT_FOUND(404, "AUTH002", "KAKAO OAuth 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(404,"AUTH003","KAKAO OAuth Refresh 토큰이 만료되었습니다." ),
    FCM_TOKEN_NOT_FOUND(404,"AUTH004","FCM 토큰값을 찾을 수 없습니다."),


    //USER
    ALREADY_EXIST_USER(403, "USER001", "이미 존재하는 사용자입니다."),
    INVALID_USER(404, "USER002", "존재하지 않는 사용자입니다."),
    DUPLICATE_NICKNAME(409, "USER003", "이미 존재하는 닉네임입니다."),

    // 닉네임 관련
    INVALID_NICKNAME_LENGTH(407, "USER003", "닉네임은 1자 이상 10자 이하이어야 합니다."),
    INVALID_NICKNAME_FORMAT(407, "USER004", "닉네임은 한글, 영어, 숫자만 사용할 수 있습니다."),

    // ROLE
    ACCESS_DENIED(406, "ROLE001", "접근 권한이 없습니다."),

    //MATCH
    MATCH_NOT_FOUND(408,"MATCH001","해당 경기 정보를 찾을 수 없습니다."),
    MATCH_RESULT_FORMAT_INVALID(413,"MATCH002", "경기 결과 형식이 잘못되었습니다. 예: 5:3"),


    //MATCH_RECORD
    NOT_FOUND_RECORD(408,"RECORD001","해당 직관기록을 찾을 수 없습니다."),
    RECORD_NOT_OWNED_DELETE(409, "RECORD002", "본인이 작성한 기록만 삭제 할 수 있습니다."),

    RECORD_NOT_OWNED(409, "RECORD003", "본인이 작성한 기록만 접근할 수 있습니다."),

    //IMAGE
    FILE_UPLOAD_FAIL(410, "IMAGE001", "사진 업로드 실패입니다."),
    INVALID_FILE_EXTENSION(411,"FILE_001", "유효하지 않은 파일 형식입니다."),
    UNSUPPORTED_FILE_EXTENSION(411,"FILE_002", "지원하지 않는 파일 확장자입니다."),
    INVALID_URL(412, "URL001", "유효하지 않은 URL입니다."),

    //ALERT
    ALERT_NOT_FOUND(414, "ALERT_001", "해당 사용자의 알림 설정이 존재하지 않습니다."),

    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");


    private int status;
    private final String code;
    private final String message;

    /**
     * 전체 ErrorCode 리스트를 반환하는 메서드
     */
    public static List<ErrorCode> getAllErrorCodes() {
        return Arrays.stream(ErrorCode.values()).collect(Collectors.toList());
    }
}