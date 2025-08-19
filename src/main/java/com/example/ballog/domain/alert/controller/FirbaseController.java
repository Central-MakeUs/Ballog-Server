package com.example.ballog.domain.alert.controller;

import com.example.ballog.domain.alert.dto.request.FcmTokenRequest;
import com.example.ballog.domain.login.entity.FcmToken;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.FcmTokenRepository;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.ApiErrorResponse;
import com.example.ballog.global.common.message.ApiErrorResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
@Tag(name = "Fcm", description = "Fcm API")
public class FirbaseController {

    private final FcmTokenRepository fcmTokenRepository;

    @PostMapping("/register-token")
    @Operation(summary = "fcm 토큰값 DB 저장", description = "fcm 토큰값 DB 저장 API")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED)
    })
    public ResponseEntity<Void> registerFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FcmTokenRequest request) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        User user = userDetails.getUser();

        Optional<FcmToken> existingToken = fcmTokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            FcmToken token = existingToken.get();
            token.setDeviceToken(request.getToken());
            fcmTokenRepository.save(token);
        } else {
            FcmToken token = new FcmToken();
            token.setUser(user);
            token.setDeviceToken(request.getToken());
            fcmTokenRepository.save(token);
        }
        return ResponseEntity.ok().build();
    }
}


