package com.example.ballog.domain.login.controller;

import com.example.ballog.domain.login.dto.request.SignupRequest;
import com.example.ballog.domain.login.dto.response.KakaoOAuthTokenResponse;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.login.service.OAuthTokenService;
import com.example.ballog.domain.login.service.TokenService;
import com.example.ballog.domain.login.service.UserService;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
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
import org.springframework.http.MediaType;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;
    private final OAuthTokenService oAuthTokenService;

    @Value("${kakao.admin-key}")
    private String adminKey;

    @PostMapping("/login/kakao")
    @Operation(summary = "카카오 회원가입 및 로그인", description = "카카오 회원가입 및 로그인 처리")
    public ResponseEntity<BasicResponse<Object>> kakaoSignup(
            @RequestParam(name = "code") String code) {

        try {
            KakaoOAuthTokenResponse fullToken = oAuthTokenService.getFullKakaoTokenResponse(code);
            String accessToken = fullToken.getAccessToken();

            User kakaoUser = oAuthTokenService.getKakaoUser(accessToken);
            User user = userService.findByEmail(kakaoUser.getEmail());

            if (user == null) {
                // 1차 회원가입 -> 카카오에서 주는 정보 저장
                User newUser = new User();
                newUser.setKakaoId(kakaoUser.getKakaoId());
                newUser.setEmail(kakaoUser.getEmail());

                User savedUser = userService.signup(newUser);
                oAuthTokenService.saveKakaoToken(savedUser, fullToken);
                return userService.processLogin(savedUser, true);
            }

            // 이미 가입된 사용자인 경우 -> 로그인 처리
            oAuthTokenService.saveKakaoToken(user, fullToken);
            return userService.processLogin(user, false);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("처리 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


    @PostMapping("/signup")
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

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 API")
    public ResponseEntity<BasicResponse<String>> logout(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader("Authorization");

            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(BasicResponse.ofFailure("Authorization 헤더가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED));
            }

            String accessToken = bearerToken.substring(7);
            Long userId = tokenService.extractUserIdFromAccessToken(accessToken);

            User user = userService.findById(userId);
            String kakaoAccessToken = oAuthTokenService.getAccessTokenByUser(user);

            oAuthTokenService.logoutFromKakao(kakaoAccessToken);
            oAuthTokenService.invalidateTokensByUser(user);

            return ResponseEntity.ok(BasicResponse.ofSuccess("로그아웃 성공"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @DeleteMapping("/withdraw")
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

            User user = userService.findById(userId);
            oAuthTokenService.unlinkFromKakao(user);
            userService.withdraw(userId);

            return ResponseEntity.ok(BasicResponse.ofSuccess("회원탈퇴 성공"));
        }  catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.REFRESH_TOKEN_EXPIRED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(BasicResponse.ofFailure("재로그인이 필요합니다. 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BasicResponse.ofFailure(e.getMessage(), HttpStatus.BAD_REQUEST));
        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(BasicResponse.ofFailure(e.getMessage(), HttpStatus.BAD_REQUEST));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BasicResponse.ofFailure("회원탈퇴 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


}
