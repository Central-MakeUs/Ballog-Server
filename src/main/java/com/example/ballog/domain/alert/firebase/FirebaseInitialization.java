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

    public synchronized void initialize() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                logger.info("FirebaseApp 이미 초기화됨");
                return;
            }

            String fixedConfig = firebaseConfig.replace("\\n", "\n");
            InputStream serviceAccount = new ByteArrayInputStream(fixedConfig.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            logger.info("FirebaseApp 초기화 성공");
        } catch (IOException e) {
            logger.error("Firebase 초기화 실패", e);
        }
    }

    @PostConstruct
    public void postConstruct() {
        initialize();
    }
}