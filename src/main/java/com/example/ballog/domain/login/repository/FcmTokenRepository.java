package com.example.ballog.domain.login.repository;

import com.example.ballog.domain.login.entity.FcmToken;
import com.example.ballog.domain.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserAndDeviceToken(User user, String deviceToken);
    Optional<FcmToken> findByUser(User user);
    void deleteAllByUserUserId(Long userId);
}
