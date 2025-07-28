package com.example.ballog.domain.login.service;


import com.example.ballog.domain.login.entity.OAuthToken;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.OAuthTokenRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthTokenService { //Kakao & Apple

    private final OAuthTokenRepository oAuthTokenRepository;

    public OAuthToken getTokenByUserAndProvider(User user, String provider) {
        return oAuthTokenRepository.findByUserAndProvider(user, provider)
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND));
    }

}
