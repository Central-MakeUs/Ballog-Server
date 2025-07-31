package com.example.ballog.domain.login.repository;

import com.example.ballog.domain.login.entity.RefreshToken;
import com.example.ballog.domain.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    RefreshToken findByUser(User user);
    Optional<RefreshToken> findByUserUserId(Long userId);
}
