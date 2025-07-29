package com.example.ballog.domain.alert.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {
    private String token; //사용자 디바이스 토큰
}