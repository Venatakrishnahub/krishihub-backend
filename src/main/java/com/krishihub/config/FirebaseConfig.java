



package com.krishihub.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config-file:firebase-service-account.json}")
    private String configFile;

    // Railway will set this env var; locally it's blank
    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseJsonEnv;

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseMessaging.getInstance(FirebaseApp.getInstance());
            }

            InputStream is;

            if (firebaseJsonEnv != null && !firebaseJsonEnv.isBlank()) {
                // ✅ Railway: load from environment variable
                log.info("Loading Firebase config from environment variable");
                is = new ByteArrayInputStream(firebaseJsonEnv.getBytes(StandardCharsets.UTF_8));
            } else {
                // ✅ Local: load from file in resources/
                log.info("Loading Firebase config from file: {}", configFile);
                is = new ClassPathResource(configFile).getInputStream();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(is))
                    .build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase initialised successfully");
            return FirebaseMessaging.getInstance(app);

        } catch (IOException e) {
            log.warn("Firebase config '{}' not found — push notifications disabled. " +
                    "App will start normally without FCM.", configFile);
            return null;
        }
    }
}