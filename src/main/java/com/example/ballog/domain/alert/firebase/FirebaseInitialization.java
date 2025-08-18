package com.example.ballog.domain.alert.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Profile("!test")
public class FirebaseInitialization {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseInitialization.class);


    @Value("${firebase.config}")
    private String firebaseConfig;

    @PostConstruct
    public void initialize() {
        try {
            // JSON 문자열을 InputStream으로 변환
            InputStream serviceAccount = new ByteArrayInputStream(firebaseConfig.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("FirebaseApp 초기화 성공");
            } else {
                logger.info("FirebaseApp 이미 초기화됨");
            }
        } catch (IOException e) {
            logger.error("Firebase 초기화 실패: {}", e.getMessage(), e);
        }
    }


//    @Value("${firebase.service-account.path}")
//    private String serviceAccountPath;
//
//    @PostConstruct
//    public void initialize() {
//        try {
//            InputStream serviceAccount = new ClassPathResource(serviceAccountPath).getInputStream();
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build();
//
//            if (FirebaseApp.getApps().isEmpty()) {
//                FirebaseApp.initializeApp(options);
//                logger.info("FirebaseApp 초기화 성공");
//            } else {
//                logger.info("FirebaseApp 이미 초기화됨");
//            }
//        } catch (IOException e) {
//            logger.error("Firebase 초기화 실패: {}", e.getMessage(), e);
//        }
//    }


}
