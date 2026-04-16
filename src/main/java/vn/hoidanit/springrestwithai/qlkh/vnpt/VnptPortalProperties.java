package vn.hoidanit.springrestwithai.qlkh.vnpt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình SOAP PortalService VNPT (theo docs mẫu SOAP 1.1 — {@code downloadInvErrorFkey}).
 * Giá trị được bind từ application.yml → biến môi trường VNPT_PORTAL_* (nạp qua main() bằng dotenv).
 */
@ConfigurationProperties(prefix = "vnpt.portal")
public class VnptPortalProperties {

    /**
     * URL đầy đủ tới {@code PortalService.asmx}, ví dụ
     * {@code https://...vnpt-invoice.com.vn/PortalService.asmx}.
     */
    private String serviceUrl = "";

    private String username = "";

    private String password = "";

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl != null ? serviceUrl.trim() : "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password != null ? password : "";
    }

    public boolean isConfigured() {
        return !serviceUrl.isEmpty() && !username.isEmpty() && !password.isEmpty();
    }
}
