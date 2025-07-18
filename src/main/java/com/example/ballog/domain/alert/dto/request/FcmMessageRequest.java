package com.example.ballog.domain.alert.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmMessageRequest {
    private String token;
    private String title;
    private String body;
}
