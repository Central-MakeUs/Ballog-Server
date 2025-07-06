package com.example.ballog.domain.matchrecord.controller;

import com.example.ballog.domain.login.entity.Role;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.matchrecord.dto.request.MatchRecordRequest;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordResponse;
import com.example.ballog.domain.matchrecord.service.MatchRecordService;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/record")
@Tag(name = "Record", description = "Record API")
public class MatchRecordController {
    private final MatchRecordService matchRecordService;

    @PostMapping
    @Operation(summary = "직관 기록 등록", description = "사용자가 직관 경기 실시간 기록 등록")
    public ResponseEntity<BasicResponse<MatchRecordResponse>> createRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchRecordRequest request) {

        if (userDetails == null || userDetails.getUser().getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        MatchRecordResponse response = matchRecordService.createRecord(request, userDetails.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BasicResponse.ofSuccess("직관 기록 등록 성공", HttpStatus.OK.value(),  response));
    }
}
