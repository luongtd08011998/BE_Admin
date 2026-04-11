package vn.hoidanit.springrestwithai.config;

import java.nio.file.Path;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.upload")
public class FileUploadProperties {

    private String baseDir;
    private long maxFileSize;
    private List<String> allowedExtensions;
    private List<String> allowedFolders;

    public String getBaseDir() {
        return baseDir;
    }

    /**
     * Thư mục gốc lưu upload (đường dẫn tuyệt đối). Tránh lệch CWD giữa ghi file và {@code ResourceHandler}.
     */
    public Path getUploadRoot() {
        if (baseDir == null || baseDir.isBlank()) {
            throw new IllegalStateException("app.upload.base-dir must be set");
        }
        return Path.of(baseDir).toAbsolutePath().normalize();
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getAllowedFolders() {
        return allowedFolders;
    }

    public void setAllowedFolders(List<String> allowedFolders) {
        this.allowedFolders = allowedFolders;
    }
}
