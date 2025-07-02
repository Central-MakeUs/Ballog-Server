package com.example.ballog.domain.login.controller;

import com.example.ballog.domain.login.dto.request.SignupRequest;
import com.example.ballog.domain.login.dto.request.UpdateUserRequest;
import com.example.ballog.domain.login.dto.response.UserInfoResponse;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.login.service.TokenService;
import com.example.ballog.domain.login.service.UserService;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Authentication API")
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;

    @Value("${kakao.admin-key}")
    private String adminKey;

    @PostMapping("/auth/login/kakao")
    @Operation(summary = "카카오 회원가입 및 로그인", description = "카카오 회원가입 및 로그인 처리")
    public ResponseEntity<BasicResponse<Object>> kakaoSignup(
            @RequestParam(name = "code") String code) {

        try {
            String accessToken = userService.getKakaoAccessToken(code);
            User kakaoUser = userService.getKakaoUser(accessToken);
            User user = userService.findByEmail(kakaoUser.getEmail());

            if (user == null) {
                //1차 회원가입 -> 카카오에서 주는 정보 저장
                User newUser = new User();
                newUser.setKakaoId(kakaoUser.getKakaoId());
                newUser.setEmail(kakaoUser.getEmail());

                User savedUser = userService.signup(newUser);
                return userService.processLogin(savedUser, true);

            }
            // 이미 가입된 사용자인 경우 -> 로그인 처리
            return userService.processLogin(user, false);


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("처리 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


    @PostMapping("/auth/signup")
    @Operation(summary = "회원가입시 추가 정보 저장", description = "추가정보인 응원팀과 닉네임 정보를 저장")
    public ResponseEntity<BasicResponse<String>> completeSignup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SignupRequest request) {

        try {
            User user = userDetails.getUser();

            userService.validateNickname(request.getNickname());

            user.setBaseballTeam(request.getBaseballTeam());
            user.setNickname(request.getNickname());
            user.setIsNewUser(false);

            userService.updateUser(user);

            return ResponseEntity.ok(BasicResponse.ofSuccess("회원가입이 완료되었습니다."));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 처리")
    public ResponseEntity<BasicResponse<String>> logout(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader("Authorization");

            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(BasicResponse.ofFailure("Authorization 헤더가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED));
            }

            String accessToken = bearerToken.substring(7);
            Long userId = tokenService.extractUserIdFromAccessToken(accessToken);
            userService.invalidateRefreshTokenByUserId(userId);

            return ResponseEntity.ok(BasicResponse.ofSuccess("로그아웃 성공"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @DeleteMapping("/auth/withdraw")
    @Operation(summary = "회원탈퇴", description = "회원탈퇴 API")
    public ResponseEntity<BasicResponse<String>> withdraw(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader("Authorization");

            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(BasicResponse.ofFailure("Authorization 헤더가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED));
            }

            String accessToken = bearerToken.substring(7);
            Long userId = tokenService.extractUserIdFromAccessToken(accessToken);
            userService.withdraw(userId);

            return ResponseEntity.ok(BasicResponse.ofSuccess("회원탈퇴 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BasicResponse.ofFailure(e.getMessage(), HttpStatus.BAD_REQUEST));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("회원탈퇴 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }



    //마이페이지 -> 유저정보 수정
    @PatchMapping("/user/mypage")
    @Operation(summary = "회원정보수정", description = "마이페이지에서 회원 정보 수정처리 - 응원팀&닉네임")
    public ResponseEntity<BasicResponse<String>> updateUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateUserRequest request) {
        try {

            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(BasicResponse.ofFailure("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED));
            }

            Long userId = userDetails.getUser().getUserId();
            userService.updateUser(userId, request);

            return ResponseEntity.ok(BasicResponse.ofSuccess("회원 정보 수정 완료"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


    @GetMapping("/user/mypage")
    @Operation(summary = "회원정보 조회", description = "마이페이지 -> 회원 정보 조회")
    public ResponseEntity<BasicResponse<UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(BasicResponse.ofFailure("인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED));
        }

        User user = userDetails.getUser();

        UserInfoResponse response = new UserInfoResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getBaseballTeam().name(),
                user.getIsNewUser(),
                user.getRole().name()
        );

        return ResponseEntity.ok(BasicResponse.ofSuccess(response));
    }


}
