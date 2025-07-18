package com.example.ballog.domain.alert.controller;

import com.example.ballog.domain.alert.dto.request.FcmMessageRequest;
import com.example.ballog.domain.alert.dto.request.FcmTokenRequest;
import com.example.ballog.domain.alert.service.FirebaseMessageService;
import com.example.ballog.domain.login.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm")
@Tag(name = "Fcm", description = "Fcm API")
public class FirbaseController {

    private final FirebaseMessageService firebaseMessageService;
    private final UserService userService;

    @PostMapping("/register-token")
    public ResponseEntity<Void> registerFcmToken(@RequestBody FcmTokenRequest request) {
        userService.saveFcmToken(request.getUserId(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody FcmMessageRequest request) {
        String response = firebaseMessageService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

}


