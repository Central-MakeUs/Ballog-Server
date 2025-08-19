package com.example.ballog.domain.alert.firebase;

import com.example.ballog.domain.alert.dto.request.FcmMessageRequest;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseMessageService {


    private static final Logger log = LoggerFactory.getLogger(FirebaseMessageService.class);

    private final FirebaseInitialization firebaseInitialization;

    public String sendMessage(FcmMessageRequest request) {
        // Firebase 초기화 확인
        if (FirebaseApp.getApps().isEmpty()) {
            firebaseInitialization.initialize();
        }
        System.out.println("전송할 FCM 토큰: " + request.getToken());

        log.info("FCM 발송 요청 - token={}, title={}, body={}",
                request.getToken(),
                request.getNotification().getTitle(),
                request.getNotification().getBody());


        Message message = Message.builder()
                .setToken(request.getToken())
                .setNotification(Notification.builder()
                        .setTitle(request.getNotification().getTitle())
                        .setBody(request.getNotification().getBody())
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 발송 성공 - response={}", response);
            return "알림 메시지 전달 성공: " + response;
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            log.error("FCM 발송 실패 - token={}", request.getToken(), e);
            return "알림 메시지 전달 실패";
        }
    }

}


