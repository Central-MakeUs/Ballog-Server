package com.example.ballog.domain.match.controller;

import com.example.ballog.domain.login.entity.Role;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.match.dto.request.MatchesRequest;
import com.example.ballog.domain.match.service.MatchesService;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/match")
@Tag(name = "Match", description = "Match Schedule API")
public class MatchesController {

    private final MatchesService matchesService;

    @PostMapping
    @Operation(summary = "경기일정 등록", description = "관리자만 경기일정을 등록할 수 있습니다.")
    public ResponseEntity<BasicResponse<String>> createMatch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MatchesRequest request) {

        if (userDetails == null || userDetails.getUser().getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        matchesService.createMatches(request);
        return ResponseEntity.ok(BasicResponse.ofSuccess("경기일정이 등록 성공"));
    }

}
