package com.example.ballog.domain.login.repository;

import com.example.ballog.domain.login.entity.OAuthToken;
import com.example.ballog.domain.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findByUser(User user);
    List<OAuthToken> findAllByUserUserId(Long userId);
    Optional<OAuthToken> findByUserAndProvider(User user, String provider);
    Optional<OAuthToken> findByProviderAndProviderId(String provider, String providerId);


}
