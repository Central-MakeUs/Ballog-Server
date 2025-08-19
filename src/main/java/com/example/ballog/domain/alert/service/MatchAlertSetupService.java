package com.example.ballog.domain.alert.service;

import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.alert.scheduler.FcmSchedulerService;
import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.domain.match.entity.Matches;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchAlertSetupService {

    private final FcmSchedulerService schedulerService;
    private final AlertRepository alertRepository;

    /**
     * 경기 등록 시 모든 알림 Job 등록
     */
    public void scheduleUserAlertsForMatch(Matches match) {
        LocalDateTime matchDateTime = LocalDateTime.of(match.getMatchesDate(), match.getMatchesTime());


        // start_alert: 경기 10분 전 알림
        List<Long> usersForStartAlert = alertRepository.findUserIdsByTeamAndAlertTrue(
                match.getHomeTeam(), match.getAwayTeam(), "start_alert"
        );
        if (!usersForStartAlert.isEmpty()) {
            LocalDateTime tenMinutesBefore = matchDateTime.minusMinutes(10);
            schedulerService.scheduleAlertJob(match, "start_alert", tenMinutesBefore, usersForStartAlert);
        }

        // in_game_alert: 경기 1시간 후
        List<Long> usersForInGameAlert = alertRepository.findUserIdsByTeamAndAlertTrue(
                match.getHomeTeam(), match.getAwayTeam(), "in_game_alert"
        );
        if (!usersForInGameAlert.isEmpty()) {
            LocalDateTime oneHourLater = matchDateTime.plusHours(1);
            schedulerService.scheduleAlertJob(match, "in_game_alert", oneHourLater, usersForInGameAlert);
        }
    }
}
