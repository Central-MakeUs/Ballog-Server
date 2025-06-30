package com.example.ballog.domain.login.dto.request;

import lombok.Data;

@Data
public class KakaoTokenRequest {
    private String refreshToken;
}
