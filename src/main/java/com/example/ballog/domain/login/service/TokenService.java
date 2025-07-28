package com.example.ballog.domain.login.service;

import com.example.ballog.domain.login.entity.RefreshToken;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.RefreshTokenRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

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
                .setSubject("access-token")
                .claim("userId", user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600000)) //1주일
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // RefreshToken 저장
    @Transactional
    public void saveRefreshToken(User user, String refreshToken) {
        Optional<RefreshToken> existing = refreshTokenRepository.findByUserUserId(user.getUserId());
        if (existing.isPresent()) {
            RefreshToken token = existing.get();
            token.setRefreshToken(refreshToken);
            refreshTokenRepository.save(token);
        } else {
            RefreshToken newToken = new RefreshToken();
            newToken.setUser(user);
            newToken.setRefreshToken(refreshToken);
            refreshTokenRepository.save(newToken);
        }
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

    public Long extractUserIdFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        String subject = claims.getSubject();

        if (subject == null) {
            throw new RuntimeException("토큰에 subject가 없습니다.");
        }

        return Long.valueOf(subject);
    }

}
