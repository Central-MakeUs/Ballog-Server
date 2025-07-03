package com.example.ballog.domain.login.service;

import com.example.ballog.domain.login.dto.request.UpdateUserRequest;
import com.example.ballog.domain.login.dto.response.KakaoTokenResponse;
import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.RefreshTokenRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.BasicResponse;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUrl;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUrl;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.admin-key}")
    private String adminKey;

    // code로 accessToken 받기
    public String getKakaoAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", redirectUri);
        params.add("client_secret", kakaoClientSecret);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                kakaoTokenUrl, HttpMethod.POST, request, KakaoTokenResponse.class);

        return response.getBody().getAccessToken();
    }


    // 카카오에서 사용자 정보 가져오기
    public User getKakaoUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                kakaoUserInfoUrl, HttpMethod.GET, request, String.class);

        // JSON 응답 본문에서 직접 정보 추출
        String responseBody = response.getBody();
        if (responseBody != null) {

            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject kakaoAccount = jsonObject.getAsJsonObject("kakao_account");

            Long kakaoId = jsonObject.get("id").getAsLong();
            String email = kakaoAccount.get("email").getAsString();

            User user = new User();
            user.setKakaoId(kakaoId);
            user.setEmail(email);

            return user;
        }
        throw new RuntimeException("유저의 정보가 없습니다.");
    }

    public User signup(User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void validateNickname(String nickname) {
        if (nickname == null || nickname.length() < 1 || nickname.length() > 10) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME_LENGTH);
        }

        if (!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
        }

        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }


    public void updateUser(User user) {
        userRepository.save(user);
    }

    public ResponseEntity<BasicResponse<Object>> processLogin(User user, boolean isSignup) {
        String refreshToken = tokenService.getRefreshToken(user);

        if (refreshToken == null || refreshToken.isEmpty()) { //새로 회원가입하는 유저 or 로그아웃하고 로그인하는 유저
            refreshToken = tokenService.createRefreshToken(user);
            tokenService.saveRefreshToken(user, refreshToken);
        }

        String accessToken = tokenService.renewAccessToken(refreshToken);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken + ", Refresh " + refreshToken);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("accessToken", accessToken);
        responseData.put("refreshToken", refreshToken);

        String message = isSignup ? "회원가입 성공" : "로그인 성공";

        return ResponseEntity.ok()
                .headers(headers)
                .body(BasicResponse.ofSuccess(responseData, message));
    }

    @Transactional
    public void invalidateRefreshTokenByUserId(Long userId) {
        refreshTokenRepository.findByUserUserId(userId).ifPresent(refreshToken -> {
            refreshToken.setRefreshToken(null);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void withdraw(Long userId) {

        refreshTokenRepository.findByUserUserId(userId)
                .ifPresent(refreshTokenRepository::delete);

        userRepository.findById(userId)
                .ifPresent(userRepository::delete);
    }


    @Transactional
    public void updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

        if (request.getNickname() != null) {
            validateNickname(request.getNickname());
            user.setNickname(request.getNickname());
        }

        if (request.getBaseballTeam() != null) {
            user.setBaseballTeam(BaseballTeam.valueOf(request.getBaseballTeam()));
        }
    }


}
