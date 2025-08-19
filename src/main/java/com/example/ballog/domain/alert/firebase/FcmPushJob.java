package com.example.ballog.domain.alert.firebase;

import com.example.ballog.domain.alert.dto.request.FcmMessageRequest;
import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.login.entity.FcmToken;
import com.example.ballog.domain.login.repository.FcmTokenRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.domain.login.entity.User;
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

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        Long matchId = dataMap.getLong("matchId");
        String alertType = dataMap.getString("alertType");
        List<Long> userIds = (List<Long>) dataMap.get("userIds");

        List<User> users = userRepository.findAllById(userIds);

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
}
