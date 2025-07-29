package com.example.ballog.domain.alert.service;

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

    private final UserRepository userRepository;
    private final FcmSchedulerService schedulerService;

    public void scheduleUserAlertsForMatch(Matches match) {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            BaseballTeam userTeam = user.getBaseballTeam();
            if (userTeam == null || userTeam == BaseballTeam.NONE)
                continue;

            if (userTeam.equals(match.getHomeTeam()) || userTeam.equals(match.getAwayTeam())) {
                LocalDateTime matchDateTime = LocalDateTime.of(match.getMatchesDate(), match.getMatchesTime());

                LocalDateTime tenMinutesBefore = matchDateTime.minusMinutes(10);
                schedulerService.scheduleAlertJob(user, match, "start_alert", tenMinutesBefore);

                LocalDateTime oneHourLater = matchDateTime.plusHours(1);
                schedulerService.scheduleAlertJob(user, match, "in_game_alert", oneHourLater);
            }
        }
    }
}
