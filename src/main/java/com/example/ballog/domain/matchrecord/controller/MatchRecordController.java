package com.example.ballog.domain.matchrecord.controller;

import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.matchrecord.dto.request.MatchRecordRequest;
import com.example.ballog.domain.matchrecord.dto.request.MatchResultRequest;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordDetailResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordListResponse;
import com.example.ballog.domain.matchrecord.dto.response.MatchRecordResponse;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
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
            @ApiErrorResponse(ErrorCode.MATCH_NOT_FOUND)
    })
    public ResponseEntity<BasicResponse<MatchRecordResponse>> createRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchRecordRequest request) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        MatchRecordResponse response = matchRecordService.createRecord(request, userDetails.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BasicResponse.ofSuccess("직관 기록 등록 성공", HttpStatus.OK.value(),  response));
    }

    @PatchMapping("/{recordId}/result")
    @Operation(summary = "직관 기록 결과 입력", description = "기록 종료 시 경기 결과(WIN, LOSS, DRAW)를 입력")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.NOT_FOUND_RECORD)
    })
    public ResponseEntity<BasicResponse<Void>> updateResult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("recordId") Long recordId,
            @RequestBody MatchResultRequest request) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        matchRecordService.updateResult(recordId, request.getResult());

        return ResponseEntity.ok(BasicResponse.ofSuccess("경기 결과 입력 성공", HttpStatus.OK.value(), null));
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "직관 기록 상세 조회", description = "특정 직관 기록 상세 정보 조회")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.NOT_FOUND_RECORD),
            @ApiErrorResponse(ErrorCode.RECORD_NOT_OWNED)
    })
    public ResponseEntity<BasicResponse<MatchRecordDetailResponse>> getRecordDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("recordId") Long recordId) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        MatchRecordDetailResponse response = matchRecordService.getRecordDetail(recordId, userDetails.getUser());

        return ResponseEntity.ok(
                BasicResponse.ofSuccess("직관 기록 상세 조회 성공", HttpStatus.OK.value(), response));
    }

    @GetMapping
    @Operation(summary = "전체 직관 기록 목록 조회", description = "로그인 사용자 본인의 모든 직관 기록 조회(= 직관로그 페이지)")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.NOT_FOUND_RECORD),
            @ApiErrorResponse(ErrorCode.RECORD_NOT_OWNED)
    })
    public ResponseEntity<BasicResponse<MatchRecordListResponse>> getAllRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        MatchRecordListResponse response = matchRecordService.getAllRecordsByUser(userDetails.getUser());

        return ResponseEntity.ok(BasicResponse.ofSuccess("전체 직관 기록 목록 조회 성공", HttpStatus.OK.value(), response));
    }


    @DeleteMapping("/{recordId}")
    @Operation(summary = "직관 기록 삭제", description = "직관 기록 작성자 본인만 삭제 가능")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.NOT_FOUND_RECORD),
            @ApiErrorResponse(ErrorCode.RECORD_NOT_OWNED_DELETE)
    })
    public ResponseEntity<BasicResponse<Void>> deleteRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("recordId") Long recordId) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        MatchRecord record = matchRecordService.findById(recordId);

        if (!record.getUser().getUserId().equals(userDetails.getUser().getUserId())) {
            throw new CustomException(ErrorCode.RECORD_NOT_OWNED_DELETE);
        }

        matchRecordService.deleteRecord(recordId);

        return ResponseEntity.ok(BasicResponse.ofSuccess("직관 기록 삭제 성공", HttpStatus.OK.value(), null));
    }



}
