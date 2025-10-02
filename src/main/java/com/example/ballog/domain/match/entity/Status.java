package com.example.ballog.domain.match.entity;

public enum Status {
    SCHEDULED,   // 예정
    IN_PROGRESS, // 진행중
    COMPLETED,   // 종료
    CANCELED;    // 취소

    public static final Status DEFAULT = SCHEDULED;
}
