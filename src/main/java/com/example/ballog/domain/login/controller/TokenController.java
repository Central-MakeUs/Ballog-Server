package com.example.ballog.domain.login.controller;

import com.example.ballog.domain.login.dto.request.KakaoTokenRequest;
import com.example.ballog.domain.login.service.TokenService;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Token", description = "Token API")
public class TokenController {

    private final TokenService tokenService;

    //refreshToken으로 accessToken 재발급
    @PostMapping("/user/refresh")
    public ResponseEntity<BasicResponse<String>> refreshAccessToken(@RequestBody KakaoTokenRequest kakaoTokenRequest) {
        String refreshToken = kakaoTokenRequest.getRefreshToken();
        String newAccessToken = tokenService.renewAccessToken(refreshToken);

        if (newAccessToken == null) {
            BasicResponse<String> response = BasicResponse.ofFailure("error code : JWT003, 유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        BasicResponse<String> response = BasicResponse.ofSuccess(newAccessToken);
        return ResponseEntity.ok(response);
    }
}
