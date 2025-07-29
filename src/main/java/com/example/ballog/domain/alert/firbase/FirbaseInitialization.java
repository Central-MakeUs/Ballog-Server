package com.example.ballog.domain.alert.firbase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@Profile("!test")
public class FirbaseInitialization {

    @Value("${firebase.service-account.path}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = new ClassPathResource(serviceAccountPath).getInputStream();

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("FirebaseApp 초기화 성공");
        } catch (IOException e) {
            System.err.println("Firebase 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


