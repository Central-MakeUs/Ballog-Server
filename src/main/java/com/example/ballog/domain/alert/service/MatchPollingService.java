package com.example.ballog.domain.alert.service;

import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.alert.scheduler.FcmSchedulerService;
import com.example.ballog.domain.match.entity.Matches;
import com.example.ballog.domain.match.repository.MatchesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchPollingService {
    private final MatchesRepository matchesRepository;
    private final AlertRepository alertRepository;
    private final FcmSchedulerService schedulerService;

    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    public void pollMatchesAndSchedule() {
        LocalDateTime now = LocalDateTime.now();
        List<Matches> upcomingMatches = matchesRepository.findAllByMatchesDateAfterOrMatchesDateEquals(now.toLocalDate());

        for (Matches match : upcomingMatches) {
            scheduleMatchAlerts(match);
        }
    }

    private void scheduleMatchAlerts(Matches match) {
        LocalDateTime matchDateTime = LocalDateTime.of(match.getMatchesDate(), match.getMatchesTime());

        // start_alert: 경기 10분 전
        List<Long> usersForStartAlert = alertRepository.findUserIdsByTeamAndAlertTrue(match.getHomeTeam(), match.getAwayTeam(), "start_alert");
        if (!usersForStartAlert.isEmpty() && !schedulerService.isJobExists(match.getMatchesId(), "start_alert")) {
            LocalDateTime tenMinutesBefore = matchDateTime.minusMinutes(10);
            schedulerService.scheduleAlertJob(match, "start_alert", tenMinutesBefore, usersForStartAlert);
            log.info("[폴링] start_alert Job 등록: matchId={}, users={}", match.getMatchesId(), usersForStartAlert);
        }

        // in_game_alert: 경기 1시간 후
        List<Long> usersForInGameAlert = alertRepository.findUserIdsByTeamAndAlertTrue(match.getHomeTeam(), match.getAwayTeam(), "in_game_alert");
        if (!usersForInGameAlert.isEmpty() && !schedulerService.isJobExists(match.getMatchesId(), "in_game_alert")) {
            LocalDateTime oneHourLater = matchDateTime.plusHours(1);
            schedulerService.scheduleAlertJob(match, "in_game_alert", oneHourLater, usersForInGameAlert);
            log.info("[폴링] in_game_alert Job 등록: matchId={}, users={}", match.getMatchesId(), usersForInGameAlert);
        }
    }
}
