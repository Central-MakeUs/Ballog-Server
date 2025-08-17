package com.example.ballog.domain.alert.repository;

import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository  extends JpaRepository<Alert, Long> {
    Optional<Alert> findByUser_UserId(Long userId);

    @Query("SELECT a.user.userId FROM Alert a WHERE (a.user.baseballTeam = :homeTeam OR a.user.baseballTeam = :awayTeam) AND " +
            "(:alertType = 'start_alert' AND a.startAlert = true OR :alertType = 'in_game_alert' AND a.inGameAlert = true)")
    List<Long> findUserIdsByTeamAndAlertTrue(@Param("homeTeam") BaseballTeam homeTeam,
                                             @Param("awayTeam") BaseballTeam awayTeam,
                                             @Param("alertType") String alertType);

    Optional<Alert> findByUser(User user);

    void deleteAllByUserUserId(Long userId);

}
