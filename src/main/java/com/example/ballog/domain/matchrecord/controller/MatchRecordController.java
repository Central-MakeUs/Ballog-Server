package com.example.ballog.domain.matchrecord.controller;

import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.matchrecord.dto.request.MatchRecordRequest;;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordDetailResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordListResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchTeamEmotionResponse;
import com.example.ballog.domain.matchrecord.service.MatchRecordService;
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
@RequestMapping("/api/v1/record")
@Tag(name = "Record", description = "Record API")
public class MatchRecordController {

    private final MatchRecordService matchRecordService;

    @PostMapping
    @Operation(summary = "직관 기록 등록", description = "사용자가 직관 경기 실시간 기록 등록")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.MATCH_NOT_FOUND),
            @ApiErrorResponse(ErrorCode.ALREADY_RECORDED)
    })
    public ResponseEntity<BasicResponse<MatchRecordResponse>> createRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchRecordRequest request) {

        User user = getAuthenticatedUser(userDetails);

        MatchRecordResponse response = matchRecordService.createRecord(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BasicResponse.ofSuccess("직관 기록 등록 성공", response));
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "직관 기록 상세 조회-recordId 기반")
    public ResponseEntity<BasicResponse<MatchRecordDetailResponse>> getRecordDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("recordId") Long recordId) {

        User user = getAuthenticatedUser(userDetails);

        MatchRecordDetailResponse response = matchRecordService.getRecordDetail(recordId, user);
        return ResponseEntity.ok(BasicResponse.ofSuccess("직관 기록 상세 조회 성공", response));
    }

    @GetMapping("/{matchId}/match")
    @Operation(summary = "직관 기록 상세 조회-matchId 기반")
    public ResponseEntity<BasicResponse<MatchRecordDetailResponse>> getRecordDetailByMatchId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("matchId") Long matchId) {

        User user = getAuthenticatedUser(userDetails);

        MatchRecordDetailResponse response = matchRecordService.getRecordDetailByMatchId(matchId, user);
        return ResponseEntity.ok(BasicResponse.ofSuccess("직관 기록 상세 조회 성공 (matchId 기반)", response));
    }

    @GetMapping("/matches/{matchId}/team")
    @Operation(summary = "경기 기준 응원팀 또는 경기별 감정 통계 조회")
    public ResponseEntity<BasicResponse<MatchTeamEmotionResponse>> getTeamEmotionStatsByMatch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("matchId") Long matchId) {

        User user = getAuthenticatedUser(userDetails);
        MatchTeamEmotionResponse response = matchRecordService.getTeamEmotionStatsByMatch(matchId, user);

        return ResponseEntity.ok(BasicResponse.ofSuccess("경기 기준 응원팀/감정 통계 조회 성공", response));
    }


    @GetMapping
    @Operation(summary = "전체 직관 기록 목록 조회")
    public ResponseEntity<BasicResponse<MatchRecordListResponse>> getAllRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = getAuthenticatedUser(userDetails);

        MatchRecordListResponse response = matchRecordService.getAllRecordsByUser(user);
        return ResponseEntity.ok(BasicResponse.ofSuccess("전체 직관 기록 목록 조회 성공", response));
    }

    @DeleteMapping("/{recordId}")
    @Operation(summary = "직관 기록 삭제")
    public ResponseEntity<BasicResponse<Void>> deleteRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("recordId") Long recordId) {

        User user = getAuthenticatedUser(userDetails);
        matchRecordService.deleteRecord(recordId, user);

        return ResponseEntity.ok(BasicResponse.ofSuccess("직관 기록 삭제 성공"));
    }

    private User getAuthenticatedUser(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return userDetails.getUser();
    }
}