package vn.hoidanit.springrestwithai.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Đọc {@code .env} trước khi bind {@code application.yml} — Spring Boot không đọc .env mặc định.
 * Thứ tự tìm: {@code user.dir/.env}, rồi {@code user.dir/BE_Admin/.env} (khi chạy app từ repo cha).
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DotenvEnvironmentPostProcessor.class);
    static final String PROPERTY_SOURCE_NAME = "dotenvLocal";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = resolveEnvFile();
        if (envFile == null) {
            return;
        }
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(envFile.getParent().toString())
                    .filename(envFile.getFileName().toString())
                    .ignoreIfMalformed()
                    .load();
            Map<String, Object> map = new HashMap<>();
            dotenv.entries().forEach(e -> map.put(e.getKey(), e.getValue()));
            /*
             * MapPropertySource chỉ tra cứu khóa đúng chữ — không giống SystemEnvironmentPropertySource.
             * @ConfigurationProperties(prefix="vnpt.portal") đọc vnpt.portal.service-url, không đọc
             * trực tiếp VNPT_PORTAL_SERVICE_URL từ map này → phải ghi thêm alias.
             */
            putVnptAliases(map, dotenv);
            if (map.isEmpty()) {
                return;
            }
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, map));
            // Đồng thời set System properties cho các key quan trọng để tránh khác biệt cơ chế tra cứu giữa PropertySource và env OS.
            setSystemPropertyIfPresent(dotenv, "VNPT_PORTAL_SERVICE_URL", "vnpt.portal.service-url");
            setSystemPropertyIfPresent(dotenv, "VNPT_PORTAL_USERNAME", "vnpt.portal.username");
            setSystemPropertyIfPresent(dotenv, "VNPT_PORTAL_PASSWORD", "vnpt.portal.password");

            log.info("Đã nạp {} biến từ {}", map.size(), envFile.toAbsolutePath());
        } catch (Exception ex) {
            log.warn("Không đọc được .env tại {}: {}", envFile.toAbsolutePath(), ex.getMessage());
        }
    }

    private static Path resolveEnvFile() {
        String userDir = System.getProperty("user.dir", ".");
        Path root = Path.of(userDir);
        Path[] candidates = {
                root.resolve(".env"),
                root.resolve("BE_Admin").resolve(".env"),
        };
        for (Path p : candidates) {
            if (Files.isRegularFile(p)) {
                return p;
            }
        }
        return null;
    }

    private static void putVnptAliases(Map<String, Object> map, Dotenv dotenv) {
        putIfNonBlank(map, dotenv, "VNPT_PORTAL_SERVICE_URL", "vnpt.portal.service-url");
        putIfNonBlank(map, dotenv, "VNPT_PORTAL_USERNAME", "vnpt.portal.username");
        putIfNonBlank(map, dotenv, "VNPT_PORTAL_PASSWORD", "vnpt.portal.password");
    }

    private static void putIfNonBlank(Map<String, Object> map, Dotenv dotenv, String envKey, String propertyKey) {
        String v = dotenv.get(envKey);
        if (v == null) {
            return;
        }
        String t = v.trim();
        if (t.isEmpty()) {
            return;
        }
        map.put(propertyKey, t);
    }

    private static void setSystemPropertyIfPresent(Dotenv dotenv, String envKey, String propertyKey) {
        String v = dotenv.get(envKey);
        if (v == null) {
            return;
        }
        String t = v.trim();
        if (t.isEmpty()) {
            return;
        }
        // Không log giá trị (tránh lộ secret), chỉ set để Spring bind ổn định.
        System.setProperty(propertyKey, t);
    }
}
