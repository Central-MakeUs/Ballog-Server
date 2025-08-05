package com.example.ballog.domain.login.service;


import com.example.ballog.domain.Image.respository.ImageRepository;
import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.emotion.repository.EmotionRepository;
import com.example.ballog.domain.login.dto.request.SignupRequest;
import com.example.ballog.domain.login.dto.request.TermAgreeRequest;
import com.example.ballog.domain.login.entity.FcmToken;
import com.example.ballog.domain.login.entity.OAuthToken;
import com.example.ballog.domain.login.dto.request.UpdateUserRequest;
import com.example.ballog.domain.login.entity.TermAgree;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.*;
import com.example.ballog.domain.login.security.CustomUserDetails;
import com.example.ballog.domain.matchrecord.entity.MatchRecord;
import com.example.ballog.domain.matchrecord.repository.MatchRecordRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.example.ballog.global.common.message.BasicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final TokenService tokenService;
    private final AlertRepository alertRepository;
    private final TermAgreeRepository termAgreeRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final EmotionRepository emotionRepository;
    private final ImageRepository imageRepository;

    public User signup(User user) { //소셜 회원가입만 진행할때 - 1차 회원가입
        User savedUser = userRepository.save(user);
        Alert alert = new Alert();
        alert.setUser(savedUser);
        alertRepository.save(alert);
        return savedUser;
    }


    public void signup(User user, SignupRequest request) { //회원가입 - 추가정보 입력
        // 닉네임 검증
        validateNickname(request.getNickname());

        // 필수 약관 동의 여부 확인
        TermAgreeRequest termAgree = request.getTermAgree();
        if (!termAgree.isPrivacyAgree() || !termAgree.isServiceAgree()) {
            throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        user.setNickname(request.getNickname());
        user.setBaseballTeam(request.getBaseballTeam());
        user.setIsNewUser(false);
        userRepository.save(user);

        TermAgree agree = new TermAgree();
        agree.setUser(user);
        agree.setPrivacyAgree(termAgree.isPrivacyAgree());
        agree.setServiceAgree(termAgree.isServiceAgree());
        agree.setMarketingAgree(termAgree.isMarketingAgree());
        agree.setAgreedAt(LocalDateTime.now());

        termAgreeRepository.save(agree);
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

        alertRepository.deleteAllByUserUserId(userId);
        emotionRepository.deleteAllByUserUserId(userId);
        imageRepository.deleteAllByUserUserId(userId);
        termAgreeRepository.deleteAllByUserUserId(userId);
        termAgreeRepository.deleteAllByUserUserId(userId);
        matchRecordRepository.deleteAllByUserUserId(userId);
        oAuthTokenRepository.deleteAllByUserUserId(userId);
        fcmTokenRepository.deleteAllByUserUserId(userId);
        refreshTokenRepository.findByUserUserId(userId)
                .ifPresent(refreshTokenRepository::delete);
        userRepository.findById(userId)
                .ifPresent(userRepository::delete);

    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));
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
            user.setBaseballTeam(request.getBaseballTeam());
        }
    }

    @Transactional
    public void saveFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

        Optional<FcmToken> existingTokenOpt = fcmTokenRepository.findByUserAndDeviceToken(user, fcmToken);

        if (existingTokenOpt.isEmpty()) {
            FcmToken token = new FcmToken();
            token.setUser(user);
            token.setDeviceToken(fcmToken);
            fcmTokenRepository.save(token);
        }
    }


    @Transactional
    public void invalidateTokensByUser(User user) {
        OAuthToken token = oAuthTokenRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND));

        token.setAccessToken(null);
        token.setRefreshToken(null);

        oAuthTokenRepository.save(token);
    }


    public User findByAppleProviderId(String appleSub) {
        return oAuthTokenRepository.findByProviderAndProviderId("Apple", appleSub)
                .map(OAuthToken::getUser)
                .orElse(null);
    }
}
