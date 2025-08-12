package com.example.ballog.domain.emotion.controller;

import com.example.ballog.domain.emotion.dto.request.EmotionEnrollRequest;
import com.example.ballog.domain.emotion.dto.response.EmotionResponse;
import com.example.ballog.domain.emotion.service.EmotionService;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.ApiErrorResponse;
import com.example.ballog.global.common.message.ApiErrorResponses;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/emotion")
@Tag(name = "Emotion", description = "Emotion API")
public class EmotionController {

    private final EmotionService emotionService;

    @PostMapping
    @Operation(summary = "감정 표현 등록", description = "직관 기록에 대한 감정을 등록")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.NOT_FOUND_RECORD),
            @ApiErrorResponse(ErrorCode.RECORD_NOT_OWNED)
    })
    public ResponseEntity<BasicResponse<EmotionResponse>> createEmotion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody EmotionEnrollRequest request) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        EmotionResponse response = emotionService.createEmotion(request, userDetails.getUser().getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BasicResponse.ofSuccess("감정 표현 등록 성공", response));
    }

    @PostMapping("/new")
    @Operation(summary = "감정 표현 등록", description = "직관 기록에 대한 감정을 등록-연타 해결")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.NOT_FOUND_RECORD),
            @ApiErrorResponse(ErrorCode.RECORD_NOT_OWNED)
    })
    public ResponseEntity<BasicResponse<EmotionResponse>> createNewEmotion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody EmotionEnrollRequest request) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 연타 가능 + 모든 요청 DB 저장되게 처리
        EmotionResponse response = emotionService.createEmotionNew(
                request,
                userDetails.getUser().getUserId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BasicResponse.ofSuccess("감정 표현 등록 성공", response));
    }


    @GetMapping("/{recordId}")
    @Operation(summary = "감정표현 홈", description = "감정표현 홈 - 직관 기록에 대해 POSITIVE, NEGATIVE 감정 비율 조회 페이지")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.NOT_FOUND_RECORD),
            @ApiErrorResponse(ErrorCode.RECORD_NOT_OWNED)
    })
    public ResponseEntity<BasicResponse<EmotionResponse>> getEmotionRatio(
            @PathVariable("recordId") Long recordId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        EmotionResponse response = emotionService.getEmotionRatio(recordId, userDetails.getUser().getUserId());

        return ResponseEntity.ok(BasicResponse.ofSuccess("감정 비율 조회 성공",response));
    }

}
