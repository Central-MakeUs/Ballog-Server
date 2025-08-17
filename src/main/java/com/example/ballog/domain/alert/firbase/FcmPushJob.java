package com.example.ballog.domain.alert.firbase;

import com.example.ballog.domain.alert.dto.request.FcmMessageRequest;
import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.login.entity.BaseballTeam;
import com.example.ballog.domain.login.entity.FcmToken;
import com.example.ballog.domain.login.entity.OAuthToken;
import com.example.ballog.domain.login.repository.FcmTokenRepository;
import com.example.ballog.domain.login.repository.OAuthTokenRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.match.entity.Matches;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobDataMap;

import java.util.List;

@Component
public class FcmPushJob implements Job {


    @Autowired
    private FirebaseMessageService firebaseMessageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @Autowired
    private AlertRepository alertRepository;

    private static final Logger log = LoggerFactory.getLogger(FcmPushJob.class);


    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        Long matchId = dataMap.getLong("matchId");
        BaseballTeam homeTeam = BaseballTeam.valueOf(dataMap.getString("homeTeam"));
        BaseballTeam awayTeam = BaseballTeam.valueOf(dataMap.getString("awayTeam"));
        String alertType = dataMap.getString("alertType");

        log.info("[Quartz Job 실행] matchId={}, alertType={}", matchId, alertType);

        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getBaseballTeam() != null
                        && (u.getBaseballTeam().equals(homeTeam) || u.getBaseballTeam().equals(awayTeam)))
                .toList();

        for (User user : users) {
            Alert alert = alertRepository.findByUser(user).orElse(null);
            if (alert == null) continue;
            if (alertType.equals("start_alert") && !alert.getStartAlert()) continue;
            if (alertType.equals("in_game_alert") && !alert.getInGameAlert()) continue;

            FcmToken fcmToken = fcmTokenRepository.findByUser(user).orElse(null);
            if (fcmToken == null || fcmToken.getDeviceToken() == null) continue;

            String title = alertType.equals("start_alert")
                    ? user.getBaseballTeam() + " 경기 임박!"
                    : user.getBaseballTeam() + " 경기 중!";
            String body = alertType.equals("start_alert")
                    ? "볼로그와 함께 경기 볼 준비 되셨나요?"
                    : "지금 그 순간, 볼로그에 남겨볼까요?";

            FcmMessageRequest.NotificationDto notification = new FcmMessageRequest.NotificationDto(title, body);
            FcmMessageRequest request = new FcmMessageRequest(fcmToken.getDeviceToken(), notification);
            firebaseMessageService.sendMessage(request);
        }
    }



//    @Autowired
//    private FirebaseMessageService firebaseMessageService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private FcmTokenRepository fcmTokenRepository;
//
//    @Autowired
//    private AlertRepository alertRepository;
//
//
//    private static final Logger log = LoggerFactory.getLogger(FcmPushJob.class);
//
//
//    @Override
//    public void execute(JobExecutionContext context) {
//        JobDataMap dataMap = context.getMergedJobDataMap();
//        Long userId = dataMap.getLong("userId");
//        String type = dataMap.getString("type"); // "start_alert" or "in_game_alert"
//        String teamName = dataMap.getString("team");
//
//
//        log.info("[Quartz Job 실행] userId={}, type={}, team={}", userId, type, teamName);
//
//
//
//        User user = userRepository.findById(userId).orElse(null);
//        if (user == null) {
//            log.warn("사용자 없음 - userId={}", userId);
//            return;
//        }
//
//
//        Alert alert = alertRepository.findByUser(user).orElse(null);
//        if (alert == null) {
//            log.warn("알림 설정 없음 - userId={}", userId);
//            return;
//        }
//
//        if (type.equals("start_alert") && !alert.getStartAlert()) {
//            log.info("시작 알림 비활성화 - userId={}", userId);
//            return;
//        }
//        if (type.equals("in_game_alert") && !alert.getInGameAlert()) {
//            log.info("인게임 알림 비활성화 - userId={}", userId);
//            return;
//        }
//
//        FcmToken fcmToken = fcmTokenRepository.findByUser(user).orElse(null);
//        if (fcmToken == null || fcmToken.getDeviceToken() == null) {
//            log.warn("FCM 토큰 없음 - userId={}", userId);
//            return;
//        }
//
//        String title = type.equals("start_alert") ? teamName + " 경기 임박!" : teamName + " 경기 중!";
//        String body = type.equals("start_alert")
//                ? "볼로그와 함께 경기 볼 준비 되셨나요?"
//                : "지금 그 순간, 볼로그에 남겨볼까요?";
//
//        FcmMessageRequest.NotificationDto notification = new FcmMessageRequest.NotificationDto(title, body);
//        FcmMessageRequest request = new FcmMessageRequest(fcmToken.getDeviceToken(), notification);
//        firebaseMessageService.sendMessage(request);
//    }
}
