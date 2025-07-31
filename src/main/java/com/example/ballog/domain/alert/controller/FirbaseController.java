package com.example.ballog.domain.alert.controller;

import com.example.ballog.domain.alert.dto.request.FcmMessageRequest;
import com.example.ballog.domain.alert.dto.request.FcmTokenRequest;
import com.example.ballog.domain.alert.firbase.FirebaseMessageService;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.login.service.UserService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
@Tag(name = "Fcm", description = "Fcm API")
public class FirbaseController {

    private final FirebaseMessageService firebaseMessageService;
    private final UserService userService;

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

        Long userId = userDetails.getUser().getUserId();
        userService.saveFcmToken(userId, request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody FcmMessageRequest request) {
        String response = firebaseMessageService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

}


