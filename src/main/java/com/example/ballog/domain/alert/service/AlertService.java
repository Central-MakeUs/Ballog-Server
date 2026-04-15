package com.example.ballog.domain.alert.service;

import com.example.ballog.domain.alert.dto.request.AlertUpdateRequest;
import com.example.ballog.domain.alert.dto.response.AlertResponse;
import com.example.ballog.domain.alert.entity.Alert;
import com.example.ballog.domain.alert.repository.AlertRepository;
import com.example.ballog.domain.login.entity.FcmToken;
import com.example.ballog.domain.login.entity.User;
import com.example.ballog.domain.login.repository.FcmTokenRepository;
import com.example.ballog.global.common.exception.CustomException;
import com.example.ballog.global.common.exception.enums.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public AlertResponse updateAlert(Long userId, AlertUpdateRequest request) {
        Alert alert = alertRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));

        if (request.getStartAlert() != null) {
            alert.setStartAlert(request.getStartAlert());
        }

        if (request.getInGameAlert() != null) {
            alert.setInGameAlert(request.getInGameAlert());
        }

        return AlertResponse.from(alert);
    }

    public AlertResponse getAlertSettings(Long userId) {
        Alert alert = alertRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));
        return AlertResponse.from(alert);
    }


    // 로그용
    public void sendFriendRequestNotification(Long senderId, Long receiverId) {

        log.info("[FCM] 친구 요청 알림 시작 senderId={}, receiverId={}", senderId, receiverId);

        Alert alert = alertRepository.findByUser_UserId(receiverId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALERT_NOT_FOUND));

        log.info("[FCM] 알림 설정 상태={}", alert.getStartAlert());

        if (!alert.getStartAlert()) {
            log.warn("[FCM] 알림 OFF 상태라 전송 중단");
            return;
        }

        User receiver = alert.getUser();

        Optional<FcmToken> tokenOpt = fcmTokenRepository.findByUser(receiver);

        if (tokenOpt.isEmpty()) {
            log.warn("[FCM] FCM 토큰 없음 userId={}", receiver.getUserId());
            return;
        }

        String fcmToken = tokenOpt.get().getDeviceToken();
        log.info("[FCM] FCM 토큰={}", fcmToken);

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("새 친구 요청")
                        .setBody("친구 요청이 도착했어요!")
                        .build())
                .putData("senderId", senderId.toString())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM] 전송 성공 response={}", response);
        } catch (FirebaseMessagingException e) {
            log.error("[FCM] 전송 실패", e);
        }
    }


}
