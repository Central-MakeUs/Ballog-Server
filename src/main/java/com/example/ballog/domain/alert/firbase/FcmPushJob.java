package com.example.ballog.domain.alert.firbase;

import com.example.ballog.domain.alert.dto.request.FcmMessageRequest;
import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.login.entity.FcmToken;
import com.example.ballog.domain.login.entity.OAuthToken;
import com.example.ballog.domain.login.repository.FcmTokenRepository;
import com.example.ballog.domain.login.repository.OAuthTokenRepository;
import com.example.ballog.domain.login.repository.UserRepository;
import com.example.ballog.domain.login.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobDataMap;

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
        Long userId = dataMap.getLong("userId");
        String type = dataMap.getString("type"); // "start_alert" or "in_game_alert"
        String teamName = dataMap.getString("team");

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Alert alert = alertRepository.findByUser(user).orElse(null);
        if (alert == null) return;

        if (type.equals("start_alert") && !alert.getStartAlert()) return;
        if (type.equals("in_game_alert") && !alert.getInGameAlert()) return;

        FcmToken fcmToken = fcmTokenRepository.findByUser(user).orElse(null);
        if (fcmToken == null || fcmToken.getDeviceToken() == null) return;

        String title = type.equals("start_alert") ? teamName + " 경기 임박!" : teamName + " 경기 중!";
        String body = type.equals("start_alert")
                ? "볼로그와 함께 경기 볼 준비 되셨나요?"
                : "지금 그 순간, 볼로그에 남겨볼까요?";

        FcmMessageRequest.NotificationDto notification = new FcmMessageRequest.NotificationDto(title, body);
        FcmMessageRequest request = new FcmMessageRequest(fcmToken.getDeviceToken(), notification);
        firebaseMessageService.sendMessage(request);
    }
}
