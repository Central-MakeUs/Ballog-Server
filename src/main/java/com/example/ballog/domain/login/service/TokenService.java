package com.example.ballog.domain.login.service;

import com.example.ballog.domain.login.entity.RefreshToken;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.RefreshTokenRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.secret-key}")
    private String secretKey;

    // AccessToken 생성
    public String createAccessToken(User user) { //AccessToken을 JWT형식으로 생성함
        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 3600000)) //하루
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // RefreshToken 생성
    public String createRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600000)) //1주일
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // RefreshToken 저장
    public void saveRefreshToken(User user, String refreshToken) {

        RefreshToken token = new RefreshToken();

        token.setRefreshToken(refreshToken);
        token.setUser(user);
        refreshTokenRepository.save(token);
    }

    //RefreshToken 이용하여 AccessToken 재발급
    public String renewAccessToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken).orElse(null);

        if (token == null) {
            return null;
        }
        User user = token.getUser();
        return createAccessToken(user);
    }

    // RefreshToken 조회
    public String getRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user);

        if (refreshToken != null) {
            return refreshToken.getRefreshToken();
        }
        return null;
    }

    // AccessToken을 이용하여 유저 정보 조회
    public User getUserFromAccessToken(String accessToken) {
        try {
            // AccessToken에서 Claims(페이로드) 추출
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(accessToken)
                    .getBody();

            // Claims에서 이메일 추출
            String userId = claims.getSubject();

            return userRepository.findByUserId(Long.valueOf(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다. : " + userId));

        } catch (SignatureException e) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }
    }
}
