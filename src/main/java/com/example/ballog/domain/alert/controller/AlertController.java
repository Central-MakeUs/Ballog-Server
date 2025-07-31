package com.example.ballog.domain.alert.controller;

import com.example.ballog.domain.alert.dto.request.AlertUpdateRequest;
import com.example.ballog.domain.alert.dto.response.AlertResponse;
import com.example.ballog.domain.alert.service.AlertService;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.ApiErrorResponse;
import com.example.ballog.global.common.message.ApiErrorResponses;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
@Tag(name = "Alert", description = "Alert API")
public class AlertController {

    private final AlertService alertService;


    @PatchMapping("/alert")
    @Operation(summary = "마이페이지 알림설정 on/off", description = "마이페이지-> 경기 시작전&시작 후 알림설정 on/off")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED)
    })
    public ResponseEntity<BasicResponse<AlertResponse>> updateAlert(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AlertUpdateRequest request
    ) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = userDetails.getUser().getUserId();
        AlertResponse response = alertService.updateAlert(userId, request);
        return ResponseEntity.ok(BasicResponse.ofSuccess("알림 설정 성공", response));
    }
}
