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
    INVALID_TOKEN(403, "JWT003", "유효하지 않은 토큰입니다.");


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