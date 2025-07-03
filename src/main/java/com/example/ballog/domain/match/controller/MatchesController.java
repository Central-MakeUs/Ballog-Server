package com.example.ballog.domain.match.controller;

import com.example.ballog.domain.login.entity.Role;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.match.dto.request.MatchesRequest;
import com.example.ballog.domain.match.dto.response.MatchesGroupedResponse;
import com.example.ballog.domain.match.dto.response.MatchesResponse;
import com.example.ballog.domain.match.service.MatchesService;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/match")
@Tag(name = "Match", description = "Match Schedule API")
public class MatchesController {

    private final MatchesService matchesService;

    @PostMapping
    @Operation(summary = "경기일정 등록", description = "관리자만 경기일정을 등록할 수 있습니다.")
    public ResponseEntity<BasicResponse<MatchesResponse>> createMatch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchesRequest request) {

        if (userDetails == null || userDetails.getUser().getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        MatchesResponse response = matchesService.createMatches(request);
        return ResponseEntity.ok(
                BasicResponse.ofSuccess("경기일정이 등록 성공", HttpStatus.OK.value(), response)
        );
    }

    @GetMapping
    @Operation(summary = "오늘 경기 조회", description = "오늘 날짜의 경기 일정만 조회")
    public ResponseEntity<BasicResponse<List<MatchesResponse>>> getTodayMatches() {
        List<MatchesResponse> todayMatches = matchesService.getTodayMatches();
        return ResponseEntity.ok(
                BasicResponse.ofSuccess("오늘 경기 일정 조회 성공", HttpStatus.OK.value(), todayMatches)
        );
    }

    @GetMapping("/all")
    @Operation(summary = "전체 경기 일정 조회", description = "등록된 모든 경기 일정을 날짜별로 그룹화하여 조회")
    public ResponseEntity<BasicResponse<Map<String, List<MatchesGroupedResponse>>>> getAllMatchesGroupedByDate() {
        Map<String, List<MatchesGroupedResponse>> groupedMatches = matchesService.getAllMatchesGroupedByDate();
        return ResponseEntity.ok(
                BasicResponse.ofSuccess("전체 경기 일정 조회 성공", HttpStatus.OK.value(), groupedMatches)
        );
    }

    @GetMapping("/{matchId}")
    @Operation(summary = "경기 일정 상세 조회", description = "특정 경기의 상세 정보를 조회합니다.")
    public ResponseEntity<BasicResponse<MatchesResponse>> getMatchDetail(@PathVariable("matchId")  Long matchId) {
        MatchesResponse response = matchesService.getMatchDetail(matchId);
        return ResponseEntity.ok(
                BasicResponse.ofSuccess("경기 상세 조회 성공", HttpStatus.OK.value(), response)
        );
    }




}
