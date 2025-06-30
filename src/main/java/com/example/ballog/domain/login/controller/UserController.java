package com.example.ballog.domain.login.controller;

import com.example.ballog.domain.login.dto.request.SignupRequest;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.login.service.TokenService;
import com.example.ballog.domain.login.service.UserService;
import com.example.ballog.global.common.message.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Value("${kakao.admin-key}")
    private String adminKey;

    @PostMapping("/login/kakao")
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
                return userService.processLogin(newUser, true);

            }

            // 이미 가입된 사용자인 경우 -> 로그인 처리
            return userService.processLogin(user, false);


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("처리 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


    @PostMapping("/signup")
    @Operation(summary = "회원가입시 추가 정보 저장", description = "step에 따라 응원팀/닉네임 정보를 저장")
    public ResponseEntity<BasicResponse<String>> completeSignup(
            @RequestParam("step") int step,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SignupRequest request) {

        try {
            User user = userDetails.getUser();

            switch (step) {
                case 1:
                    user.setBaseballTeam(request.getBaseballTeam());
                    userService.updateUser(user);
                    return ResponseEntity.ok(BasicResponse.ofSuccess("응원팀 저장 완료"));

                case 2:
                    userService.validateNickname(request.getNickname());
                    user.setNickname(request.getNickname());
                    user.setIsNewUser(false);
                    userService.updateUser(user);
                    return ResponseEntity.ok(BasicResponse.ofSuccess("닉네임 저장 및 최종 회원가입이 완료"));

                default:
                    return ResponseEntity.badRequest().body(BasicResponse.ofFailure("잘못된 단계입니다.", HttpStatus.BAD_REQUEST));
            }

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }











}
