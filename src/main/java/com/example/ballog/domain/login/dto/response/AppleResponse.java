package com.example.ballog.domain.login.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppleResponse {
    private String id;
    private String accessToken;
    private String refreshToken;
    private String email;
}
