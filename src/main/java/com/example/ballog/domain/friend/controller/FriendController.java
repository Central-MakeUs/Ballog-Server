package com.example.ballog.domain.friend.controller;

import com.example.ballog.domain.friend.dto.response.FriendRequestResponse;
import com.example.ballog.domain.friend.dto.response.FriendResponse;
import com.example.ballog.domain.friend.service.FriendService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friend")
@Tag(name = "Friend", description = "Friend API")
public class FriendController {
    private final FriendService friendService;

    // 친구 요청
    @PostMapping("/request")
    @Operation(summary = "친구 요청", description = "닉네임 기반으로 친구 요청 보내는 API")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.INVALID_USER),
            @ApiErrorResponse(ErrorCode.FRIEND_REQUEST_NOT_ALLOWED),
            @ApiErrorResponse(ErrorCode.ALREADY_FRIEND)
    })
    public ResponseEntity<BasicResponse<Void>> requestFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body
    ){
        validateUser(userDetails);
        friendService.requestFriend(userDetails.getUser().getUserId(), body.get("nickname"));

        return ResponseEntity.status(HttpStatus.CREATED).body(BasicResponse.<Void>ofSuccess("친구 요청을 보냈어요!", null));
    }

    // 친구 수락
    @PostMapping("/accept")
    @Operation(summary = "친구 수락", description = "친구 추가 및 수락하는 API")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.FRIEND_REQUEST_NOT_FOUND)
    })
    public ResponseEntity<BasicResponse<Void>> acceptFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Map<String, Long> body){
        validateUser(userDetails);
        friendService.acceptFriend(userDetails.getUser().getUserId(), body.get("requesterId"));

        return ResponseEntity.ok(BasicResponse.<Void>ofSuccess("친구 수락 완료", null));
    }

    // 친구 거절
    @PostMapping("/reject")
    @Operation(summary = "친구 거절", description = "친구 요청을 거절하는 API")
    @ApiErrorResponses({
            @ApiErrorResponse(ErrorCode.UNAUTHORIZED),
            @ApiErrorResponse(ErrorCode.FRIEND_REQUEST_NOT_FOUND)
    })
    public ResponseEntity<BasicResponse<Void>> rejectFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Long> body
    ){
        validateUser(userDetails);
        friendService.rejectFriend(userDetails.getUser().getUserId(), body.get("requesterId"));

        return ResponseEntity.ok(BasicResponse.<Void>ofSuccess("친구 요청 거절 완료", null));
    }

    @GetMapping("/request/list")
    @Operation(summary = "받은 친구 요청 목록 조회", description = "나에게 온 친구 요청 목록을 조회")
    public ResponseEntity<BasicResponse<List<FriendRequestResponse>>> getReceivedRequests(@AuthenticationPrincipal CustomUserDetails userDetails){
        validateUser(userDetails);
        List<FriendRequestResponse> result = friendService.getReceivedRequests(userDetails.getUser().getUserId());

        return ResponseEntity.ok(BasicResponse.ofSuccess("받은 친구 요청 목록 조회 성공", result));
    }

    // 친구 목록 조회
    @GetMapping
    @Operation(summary = "친구 목록 조회", description = "친구 목록을 조회하는 API")
    public ResponseEntity<BasicResponse<List<FriendResponse>>> getFriends(@AuthenticationPrincipal CustomUserDetails userDetails){
        validateUser(userDetails);
        List<FriendResponse> friends = friendService.getFriends(userDetails.getUser().getUserId());

        return ResponseEntity.ok(BasicResponse.ofSuccess("친구 목록 조회 성공", friends));
    }

    private void validateUser(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
