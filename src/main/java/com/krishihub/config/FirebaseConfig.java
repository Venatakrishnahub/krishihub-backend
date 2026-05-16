


package com.krishihub.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() {

        try {

            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseMessaging.getInstance(FirebaseApp.getInstance());
            }

            String firebaseConfig = System.getenv("FIREBASE_CONFIG");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(
                                    new ByteArrayInputStream(
                                            firebaseConfig.getBytes(StandardCharsets.UTF_8)
                                    )
                            )
                    )
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);

            log.info("Firebase initialized successfully");

            return FirebaseMessaging.getInstance(app);

        } catch (IOException e) {

            log.warn("Firebase initialization failed", e);

            return null;
        }
    }
}