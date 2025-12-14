package com.example.ballog.domain.login.service;

import com.example.ballog.domain.login.dto.response.KakaoOAuthTokenResponse;
import com.example.ballog.domain.login.entity.OAuthToken;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.OAuthTokenRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final UserRepository userRepository;
    private final OAuthTokenRepository oAuthTokenRepository;

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


    //code로 카카오 토큰 받기
    public KakaoOAuthTokenResponse getFullKakaoTokenResponse(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", redirectUri);
        params.add("client_secret", kakaoClientSecret);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<KakaoOAuthTokenResponse> response = restTemplate.exchange(
                kakaoTokenUrl, HttpMethod.POST, request, KakaoOAuthTokenResponse.class);

        return response.getBody();
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

            String email = kakaoAccount.get("email").getAsString();

            User user = new User();
            user.setEmail(email);


            return user;
        }
        throw new RuntimeException("유저의 정보가 없습니다.");
    }
    public String getKakaoProviderUserId(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                kakaoUserInfoUrl, HttpMethod.GET, request, String.class);

        String responseBody = response.getBody();
        if (responseBody != null) {
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            return jsonObject.get("id").getAsString();
        }
        throw new RuntimeException("유저의 정보가 없습니다.");
    }

    @Transactional
    public void saveKakaoToken(User user, String accessToken, String refreshToken) {
        User savedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

        OAuthToken token = oAuthTokenRepository.findByUser(savedUser)
                .orElse(new OAuthToken());

        String providerUserId = getKakaoProviderUserId(accessToken);

        token.setUser(savedUser);
        token.setProvider("kakao");
        token.setProviderId(providerUserId);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);

        oAuthTokenRepository.save(token);
    }

    public String getAccessTokenByUser(User user) {
        return oAuthTokenRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND))
                .getAccessToken();
    }

    public void logoutFromKakao(String kakaoAccessToken) {
        String url = "https://kapi.kakao.com/v1/user/logout";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);

    }

    @Transactional
    public void unlinkFromKakao(User user) {
        User persistentUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

        String accessToken = getAccessTokenByUser(persistentUser);

        try {
            requestUnlinkToKakao(accessToken);
        } catch (HttpClientErrorException.Unauthorized e) {
            OAuthToken token = oAuthTokenRepository.findByUser(persistentUser)
                    .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND));

            KakaoOAuthTokenResponse newToken = renewAccessToken(token.getRefreshToken());
            saveKakaoToken(persistentUser, newToken.getAccessToken(), newToken.getRefreshToken());

            requestUnlinkToKakao(newToken.getAccessToken());
        }
    }

    //카카오 accesstoken만료시 재발급
    private void requestUnlinkToKakao(String accessToken) {
        String url = "https://kapi.kakao.com/v1/user/unlink";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    public KakaoOAuthTokenResponse renewAccessToken(String refreshToken) {
        try {
            String url = kakaoTokenUrl;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("client_id", kakaoClientId);
            params.add("client_secret", kakaoClientSecret);
            params.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<KakaoOAuthTokenResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, KakaoOAuthTokenResponse.class);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains("invalid_grant")) {
                throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }
            throw e;
        }
    }
}
