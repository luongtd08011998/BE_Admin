package vn.hoidanit.springrestwithai;

import java.nio.file.Files;
import java.nio.file.Path;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalProperties;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(VnptPortalProperties.class)
public class SpringRestWithAiApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringRestWithAiApplication.class);

    public static void main(String[] args) {
        loadDotenvIntoSystemProperties();
        SpringApplication.run(SpringRestWithAiApplication.class, args);
    }

    private static void loadDotenvIntoSystemProperties() {
        String userDir = System.getProperty("user.dir", ".");
        Path root = Path.of(userDir);
        Path jarDir = resolveJarDir();

        // Cho phép chỉ định đường dẫn tường minh: java -Denv.file=C:\path\.env -jar app.jar
        String explicitEnvFile = System.getProperty("env.file");

        Path[] candidates = {
                explicitEnvFile != null ? Path.of(explicitEnvFile) : null,
                jarDir != null ? jarDir.resolve(".env") : null,
                root.resolve(".env"),
                root.resolve("BE_Admin").resolve(".env"),
                root.getParent() != null ? root.getParent().resolve(".env") : null,
                root.getParent() != null ? root.getParent().resolve("BE_Admin").resolve(".env") : null,
        };
        for (Path candidate : candidates) {
            if (candidate != null && Files.isRegularFile(candidate)) {
                try {
                    Dotenv dotenv = Dotenv.configure()
                            .directory(candidate.getParent().toString())
                            .filename(candidate.getFileName().toString())
                            .ignoreIfMalformed()
                            .load();
                    dotenv.entries().forEach(e -> {
                        if (System.getProperty(e.getKey()) == null) {
                            System.setProperty(e.getKey(), e.getValue());
                        }
                    });
                    log.info("Dotenv loaded from {}", candidate.toAbsolutePath());
                } catch (Exception ex) {
                    log.warn("Không đọc được .env tại {}: {}", candidate.toAbsolutePath(), ex.getMessage());
                }
                return;
            }
        }
        log.warn("Không tìm thấy file .env (user.dir={}, jarDir={}). " +
                "Dùng: java -Denv.file=C:\\path\\.env -jar app.jar  " +
                "hoặc set biến môi trường Windows.", userDir, jarDir);
    }

    private static Path resolveJarDir() {
        try {
            Path jar = Path.of(SpringRestWithAiApplication.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            return Files.isDirectory(jar) ? jar : jar.getParent();
        } catch (Exception ex) {
            return null;
        }
    }
}
