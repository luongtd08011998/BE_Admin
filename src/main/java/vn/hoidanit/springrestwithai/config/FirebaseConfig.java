package vn.hoidanit.springrestwithai.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp already initialized — skipping.");
            return;
        }
        try (InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream("cskh-33ee2-firebase-adminsdk-fbsvc-5f457ecbb2.json")) {

            if (serviceAccount == null) {
                log.error("Firebase credentials file not found in classpath. Push notification will be disabled.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp initialized successfully.");
        } catch (IOException e) {
            log.error("Failed to initialize FirebaseApp: {}", e.getMessage(), e);
        }
    }
}
