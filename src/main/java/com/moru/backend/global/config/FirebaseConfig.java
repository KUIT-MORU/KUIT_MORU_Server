package com.moru.backend.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${fcm.service-account-file}")
    private String serviceAccountFilePath;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(serviceAccountFilePath);
            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if(FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(firebaseOptions);
            }

            System.out.println("Firebase Admin SDK 초기화 완료");
        } catch (IOException e) {
            throw new IllegalStateException("Firebase 초기화 실패", e);
        }
    }
}
