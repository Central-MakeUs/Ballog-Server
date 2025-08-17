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




//    private final UserRepository userRepository;
//    private final FcmSchedulerService schedulerService;
//    private final AlertRepository alertRepository;
//
//    public void scheduleUserAlertsForMatch(Matches match) {
//        List<User> allUsers = userRepository.findAll();
//
//        for (User user : allUsers) {
//            BaseballTeam userTeam = user.getBaseballTeam();
//            if (userTeam == null || userTeam == BaseballTeam.NONE)
//                continue;
//
//            Alert alert = alertRepository.findByUser(user).orElse(null);
//            if (alert == null) {
//                continue;
//            }
//
//            LocalDateTime matchDateTime = LocalDateTime.of(match.getMatchesDate(), match.getMatchesTime());
//
//            // start_alert가 true일 경우만 경기 시작 10분 전 알림 등록
//            if (Boolean.TRUE.equals(alert.getStartAlert())) {
//                LocalDateTime tenMinutesBefore = matchDateTime.minusMinutes(10);
//                schedulerService.scheduleAlertJob(user, match, "start_alert", tenMinutesBefore);
//            }
//
//            // in_game_alert가 true일 경우만 경기 시작 1시간 후 알림 등록
//            if (Boolean.TRUE.equals(alert.getInGameAlert())) {
//                LocalDateTime oneHourLater = matchDateTime.plusHours(1);
//                schedulerService.scheduleAlertJob(user, match, "in_game_alert", oneHourLater);
//            }
//        }
//    }
}
