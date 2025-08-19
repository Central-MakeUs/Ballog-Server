package com.example.ballog.domain.alert.service;

import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.alert.scheduler.FcmSchedulerService;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.repository.MatchesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchPollingService {
    private final MatchesRepository matchesRepository;
    private final AlertRepository alertRepository;
    private final FcmSchedulerService schedulerService;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 매일 00:00:00 실행
    public void pollNewMatches() {
        LocalDate today = LocalDate.now();
        List<Matches> newMatches = matchesRepository.findUpcomingMatchesWithNoAlerts(today);

        for (Matches match : newMatches) {
            scheduleMatchAlerts(match);
            matchesRepository.markAlertsScheduled(match.getMatchesId());
        }
    }

    private void scheduleMatchAlerts(Matches match) {
        LocalDateTime matchDateTime = LocalDateTime.of(match.getMatchesDate(), match.getMatchesTime());

        // start_alert: 경기 10분 전
        List<Long> usersForStartAlert = alertRepository.findUserIdsByTeamAndAlertTrue(match.getHomeTeam(), match.getAwayTeam(), "start_alert");
        if (!usersForStartAlert.isEmpty() && !schedulerService.isJobExists(match.getMatchesId(), "start_alert")) {
            LocalDateTime tenMinutesBefore = matchDateTime.minusMinutes(10);
            schedulerService.scheduleAlertJob(match, "start_alert", tenMinutesBefore, usersForStartAlert);
            log.info("[스케줄링] start_alert Job 등록: matchId={}, users={}", match.getMatchesId(), usersForStartAlert);
        }

        // in_game_alert: 경기 1시간 후
        List<Long> usersForInGameAlert = alertRepository.findUserIdsByTeamAndAlertTrue(match.getHomeTeam(), match.getAwayTeam(), "in_game_alert");
        if (!usersForInGameAlert.isEmpty() && !schedulerService.isJobExists(match.getMatchesId(), "in_game_alert")) {
            LocalDateTime oneHourLater = matchDateTime.plusHours(1);
            schedulerService.scheduleAlertJob(match, "in_game_alert", oneHourLater, usersForInGameAlert);
            log.info("[스케줄링] in_game_alert Job 등록: matchId={}, users={}", match.getMatchesId(), usersForInGameAlert);
        }
    }
}
