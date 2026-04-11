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
     * Thư mục gốc lưu upload (đường dẫn tuyệt đối).
     * <p>Với {@code base-dir} tương đối (vd. {@code ./uploads}): neo theo {@code user.dir} nhưng
     * nếu JVM chạy trong {@code .../target} hoặc {@code .../target/classes} thì dùng thư mục cha
     * (gốc project) để file không nằm trong {@code target/} — tránh mất khi {@code mvn clean}.
     */
    public Path getUploadRoot() {
        if (baseDir == null || baseDir.isBlank()) {
            throw new IllegalStateException("app.upload.base-dir must be set");
        }
        Path base = Path.of(baseDir.trim());
        if (base.isAbsolute()) {
            return base.normalize();
        }
        return resolveRelativeToStableWorkingDir(base).normalize();
    }

    /** Giống "thư mục project" khi dev chạy từ target/ hoặc target/classes/. */
    private static Path resolveRelativeToStableWorkingDir(Path relativeBaseDir) {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path anchor = cwd;
        while (true) {
            Path leaf = anchor.getFileName();
            if (leaf == null) {
                break;
            }
            String name = leaf.toString();
            if ("classes".equalsIgnoreCase(name)) {
                Path parent = anchor.getParent();
                if (parent != null) {
                    Path pl = parent.getFileName();
                    if (pl != null && "target".equalsIgnoreCase(pl.toString())) {
                        Path grand = parent.getParent();
                        if (grand != null) {
                            anchor = grand;
                            continue;
                        }
                    }
                }
                break;
            }
            if ("target".equalsIgnoreCase(name)) {
                Path parent = anchor.getParent();
                if (parent != null) {
                    anchor = parent;
                    continue;
                }
            }
            break;
        }
        return anchor.resolve(relativeBaseDir);
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
