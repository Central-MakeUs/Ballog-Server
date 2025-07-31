package com.example.ballog.domain.alert.firbase;

import com.example.ballog.domain.alert.dto.request.FcmMessageRequest;
import com.example.ballog.domain.login.service.UserService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseMessageService {
    public String sendMessage(FcmMessageRequest request) {
        System.out.println("전송할 FCM 토큰: " + request.getToken());

        Message message = Message.builder()
                .setToken(request.getToken())
                .setNotification(Notification.builder()
                        .setTitle(request.getNotification().getTitle())
                        .setBody(request.getNotification().getBody())
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            return "알림 메시지 전달 성공: " + response;
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "알림 메시지 전달 실패";
        }
    }

}


