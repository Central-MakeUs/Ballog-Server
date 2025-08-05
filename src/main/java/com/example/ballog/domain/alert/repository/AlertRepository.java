package com.example.ballog.domain.alert.repository;

import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository  extends JpaRepository<Alert, Long> {
    Optional<Alert> findByUser_UserId(Long userId);
    List<Alert> findAllByUserUserId(Long userId);

    List<Alert> findByStartAlertTrue();

    List<Alert> findByInGameAlertTrue();
    Optional<Alert> findByUser(User user);

    void deleteAllByUserUserId(Long userId);

}
