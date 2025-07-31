package com.example.ballog.domain.alert.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmMessageRequest { //사용자 디바이스 토큰
    private String token;
    private NotificationDto notification;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationDto {
        private String title;
        private String body;
    }
}