package com.example.ballog.domain.login.controller;

import com.example.ballog.domain.login.dto.request.SignupRequest;
import com.example.ballog.domain.login.dto.request.TermAgreeRequest;
import com.example.ballog.domain.login.dto.request.UpdateUserRequest;
import com.example.ballog.domain.login.dto.response.AppleResponse;
import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.login.entity.OAuthToken;
import com.example.ballog.domain.login.entity.Role;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.login.service.AppleOAuthService;
import com.example.ballog.domain.login.service.KakaoOAuthService;
import com.example.ballog.domain.login.service.OAuthTokenService;
import com.example.ballog.domain.login.service.UserService;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.BasicResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;



@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private KakaoOAuthService kakaoOAuthService;

    @MockBean
    private OAuthTokenService oAuthTokenService;

    @MockBean
    private AppleOAuthService appleOAuthService;


    @Test
    void kakaoLogin_성공() throws Exception { //회원가입
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        User kakaoUser = new User();
        kakaoUser.setEmail("test@kakao.com");

        User savedUser = new User();
        savedUser.setEmail("test@kakao.com");

        given(kakaoOAuthService.getKakaoUser(anyString())).willReturn(kakaoUser);
        given(userService.findByEmail(anyString())).willReturn(null);
        given(userService.signup(any(User.class))).willReturn(savedUser);
        given(userService.processLogin(any(User.class), anyBoolean()))
                .willReturn(ResponseEntity.ok(BasicResponse.ofSuccess("회원가입 성공")));

        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("회원가입 성공"));
    }

    @Test
    void kakaoLogin_실패() throws Exception {
        String accessToken = "invalidAccessToken";
        String refreshToken = "invalidRefreshToken";

        given(kakaoOAuthService.getKakaoUser(accessToken))
                .willThrow(new RuntimeException("토큰 오류"));

        mockMvc.perform(post("/api/v1/auth/login/kakao")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("X-Refresh-Token", refreshToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.success").value("카카오 로그인 처리 중 오류 발생"));
    }


    @Test
    void appleSignup_성공() throws Exception { //로그인
        String code = "valid_code";
        String email = "test@example.com";
        String appleSub = "apple_sub_123";

        AppleResponse appleResponse = new AppleResponse();
        appleResponse.setEmail(email);
        appleResponse.setId(appleSub);

        User existingUser = new User();
        existingUser.setEmail(email);

        given(appleOAuthService.getAppleInfo(code)).willReturn(appleResponse);
        given(userService.findByAppleProviderId(appleSub)).willReturn(existingUser);
        willDoNothing().given(appleOAuthService).saveAppleToken(existingUser, appleResponse);
        given(userService.processLogin(any(User.class), anyBoolean()))
                .willReturn(ResponseEntity.ok(BasicResponse.ofSuccess("로그인 성공")));

        mockMvc.perform(post("/api/v1/auth/login/apple")
                        .param("code", code)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("로그인 성공"));
    }

    @Test
    void appleSignup_실패() throws Exception {
        String code = "invalid_code";

        given(appleOAuthService.getAppleInfo(code)).willThrow(new RuntimeException("토큰 오류"));

        mockMvc.perform(post("/api/v1/auth/login/apple")
                        .param("code", code))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.success").value("애플 로그인 처리 중 오류 발생"));
    }

    @Test
    void signup_성공() throws Exception {
        TermAgreeRequest termAgreeRequest = new TermAgreeRequest();
        termAgreeRequest.setPrivacyAgree(true);
        termAgreeRequest.setServiceAgree(true);

        SignupRequest request = new SignupRequest();
        request.setNickname("구단주");
        request.setBaseballTeam(BaseballTeam.LG_TWINS);
        request.setTermAgree(termAgreeRequest);

        User user = new User();
        user.setEmail("email@test.com");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null));

        doNothing().when(userService).signup(any(User.class), any(SignupRequest.class));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("회원가입 완료"));
    }


    @Test
    void signup_실패() throws Exception {
        TermAgreeRequest termAgreeRequest = new TermAgreeRequest();
        termAgreeRequest.setPrivacyAgree(true);
        termAgreeRequest.setServiceAgree(true);

        SignupRequest request = new SignupRequest();
        request.setNickname("중복닉네임");
        request.setBaseballTeam(BaseballTeam.LG_TWINS);
        request.setTermAgree(termAgreeRequest);

        doThrow(new CustomException(ErrorCode.DUPLICATE_NICKNAME))
                .when(userService).signup(any(User.class), any(SignupRequest.class));

        User user = new User();
        user.setEmail("email@test.com");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null)
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value(ErrorCode.DUPLICATE_NICKNAME.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.DUPLICATE_NICKNAME.getCode()));
    }




    @Test
    void logout_성공() throws Exception {
        User user = new User();
        user.setUserId(1L);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        OAuthToken kakaoToken = new OAuthToken();
        kakaoToken.setAccessToken("fakeAccessToken");

        given(oAuthTokenService.getTokenByUserAndProvider(any(), eq("Kakao"))).willReturn(kakaoToken);
        given(oAuthTokenService.getTokenByUserAndProvider(any(), eq("Apple"))).willThrow(new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND));
        doNothing().when(kakaoOAuthService).logoutFromKakao(any());
        doNothing().when(userService).invalidateTokensByUser(any());
        doNothing().when(userService).invalidateRefreshTokenByUserId(anyLong());

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("로그아웃 성공"));
    }



    @Test
    void logout_실패() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(ErrorCode.UNAUTHORIZED.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void withdraw_성공() throws Exception {
        User user = new User();
        user.setUserId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );


        OAuthToken kakaoToken = new OAuthToken();
        kakaoToken.setAccessToken("fakeAccessToken");
        given(oAuthTokenService.getTokenByUserAndProvider(any(), eq("Kakao"))).willReturn(kakaoToken);

        given(oAuthTokenService.getTokenByUserAndProvider(any(), eq("Apple")))
                .willThrow(new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND));

        doNothing().when(kakaoOAuthService).unlinkFromKakao(any());

        doNothing().when(appleOAuthService).logoutFromApple(any());

        doNothing().when(userService).withdraw(anyLong());

        mockMvc.perform(delete("/api/v1/auth/withdraw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("회원탈퇴 성공"));
    }



    @Test
    void withdraw_실패() throws Exception {
        User user = new User();
        user.setUserId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        doThrow(new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED))
                .when(userService).withdraw(anyLong());

        OAuthToken kakaoToken = new OAuthToken();
        kakaoToken.setAccessToken("fakeAccessToken");
        given(oAuthTokenService.getTokenByUserAndProvider(any(), eq("Kakao"))).willReturn(kakaoToken);
        given(oAuthTokenService.getTokenByUserAndProvider(any(), eq("Apple")))
                .willThrow(new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND));

        doNothing().when(kakaoOAuthService).unlinkFromKakao(any());
        doNothing().when(appleOAuthService).logoutFromApple(any());

        mockMvc.perform(delete("/api/v1/auth/withdraw"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value("재로그인이 필요합니다. 토큰이 만료되었습니다."))
                .andExpect(jsonPath("$.message").value("fail"));
    }

    @Test
    void updateUser_성공() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("새닉네임");
        request.setBaseballTeam(BaseballTeam.DOOSAN_BEARS);

        User user = new User();
        user.setUserId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null)
        );

        doNothing().when(userService).updateUser(anyLong(), any(UpdateUserRequest.class));

        mockMvc.perform(patch("/api/v1/mypage/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value("회원 정보 수정 완료"));
    }

    @Test
    void updateUser_실패() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("잘못된 닉네임!@#");
        request.setBaseballTeam(BaseballTeam.DOOSAN_BEARS);

        User user = new User();
        user.setUserId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null)
        );

        doThrow(new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT))
                .when(userService).updateUser(anyLong(), any(UpdateUserRequest.class));

        mockMvc.perform(patch("/api/v1/mypage/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(407))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(407))
                .andExpect(jsonPath("$.error").value(ErrorCode.INVALID_NICKNAME_FORMAT.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_NICKNAME_FORMAT.getCode()));
    }

    @Test
    void getUserInfo_성공() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setNickname("구단주");
        user.setBaseballTeam(BaseballTeam.LG_TWINS);
        user.setIsNewUser(false);
        user.setRole(Role.USER);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null)
        );

        mockMvc.perform(get("/api/v1/mypage/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userId").value(user.getUserId()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                .andExpect(jsonPath("$.data.baseballTeam").value(user.getBaseballTeam().name()))
                .andExpect(jsonPath("$.data.isNewUser").value(user.getIsNewUser()))
                .andExpect(jsonPath("$.data.role").value(user.getRole().name()));
    }

    @Test
    void getUserInfo_실패() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/mypage/user"))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(ErrorCode.UNAUTHORIZED.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }
}