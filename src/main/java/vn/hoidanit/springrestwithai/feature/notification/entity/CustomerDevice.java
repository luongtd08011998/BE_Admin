package vn.hoidanit.springrestwithai.feature.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Lưu trữ FCM device token của khách hàng để push notification.
 * Tương ứng bảng {@code customer_device} trên DB primary (hr_management).
 */
@Entity
@Table(
        name = "customer_device",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_customer_device",
                columnNames = {"customer_id", "device_token"}
        )
)
public class CustomerDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "device_token", nullable = false, length = 512)
    private String deviceToken;

    /** ANDROID hoặc IOS */
    @Column(name = "platform", length = 20)
    private String platform;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Integer getCustomerId() { return customerId; }

    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getDeviceToken() { return deviceToken; }

    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }

    public String getPlatform() { return platform; }

    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
