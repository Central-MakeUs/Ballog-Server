package com.example.ballog.domain.alert.service;

import com.example.ballog.domain.alert.dto.request.FcmMessageRequest;
import com.example.ballog.domain.login.service.UserService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseMessageService {

    public String sendMessage(FcmMessageRequest request) {
        Message message = Message.builder()
                .putData("title", request.getTitle())
                .putData("content", request.getBody())
                .setToken(request.getToken())
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

