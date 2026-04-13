package vn.hoidanit.springrestwithai.qlkh.vnpt;

import java.nio.file.Files;
import java.nio.file.Path;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình SOAP PortalService VNPT (theo docs mẫu SOAP 1.1 — {@code downloadInvErrorFkey}).
 */
@ConfigurationProperties(prefix = "vnpt.portal")
public class VnptPortalProperties {
    private static final Logger log = LoggerFactory.getLogger(VnptPortalProperties.class);
    private static final Object LOAD_LOCK = new Object();
    private static volatile boolean attemptedDotenvLoad = false;

    /**
     * URL đầy đủ tới {@code PortalService.asmx}, ví dụ
     * {@code https://...vnpt-invoice.com.vn/PortalService.asmx}.
     */
    private String serviceUrl = "";

    private String username = "";

    private String password = "";

    public String getServiceUrl() {
        ensureLoadedFromDotenvIfNeeded();
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl != null ? serviceUrl.trim() : "";
    }

    public String getUsername() {
        ensureLoadedFromDotenvIfNeeded();
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username : "";
    }

    public String getPassword() {
        ensureLoadedFromDotenvIfNeeded();
        return password;
    }

    public void setPassword(String password) {
        this.password = password != null ? password : "";
    }

    public boolean isConfigured() {
        ensureLoadedFromDotenvIfNeeded();
        return !serviceUrl.isEmpty() && !username.isEmpty() && !password.isEmpty();
    }

    /**
     * Fallback an toàn: nếu Spring chưa bind được `vnpt.portal.*`, tự đọc `.env` (local/dev).
     * Tránh tình trạng IDE chạy không có biến môi trường → luôn 503.
     */
    private void ensureLoadedFromDotenvIfNeeded() {
        if (attemptedDotenvLoad) {
            return;
        }
        synchronized (LOAD_LOCK) {
            if (attemptedDotenvLoad) {
                return;
            }
            attemptedDotenvLoad = true;

            // Nếu đã có cấu hình từ YAML/env, không cần đọc .env
            if (!serviceUrl.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                return;
            }

            Path envFile = resolveEnvFile();
            if (envFile == null) {
                return;
            }
            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory(envFile.getParent().toString())
                        .filename(envFile.getFileName().toString())
                        .ignoreIfMalformed()
                        .ignoreIfMissing()
                        .load();

                String url = trimOrEmpty(dotenv.get("VNPT_PORTAL_SERVICE_URL"));
                String u = trimOrEmpty(dotenv.get("VNPT_PORTAL_USERNAME"));
                String p = trimOrEmpty(dotenv.get("VNPT_PORTAL_PASSWORD"));

                if (serviceUrl.isEmpty() && !url.isEmpty()) {
                    serviceUrl = url;
                }
                if (username.isEmpty() && !u.isEmpty()) {
                    username = u;
                }
                if (password.isEmpty() && !p.isEmpty()) {
                    password = p;
                }

                if (!serviceUrl.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                    log.info("VNPT portal config loaded from {}", envFile.toAbsolutePath());
                }
            } catch (Exception ex) {
                log.warn("Không đọc được .env cho VNPT portal: {}", ex.getMessage());
            }
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

    private static String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
