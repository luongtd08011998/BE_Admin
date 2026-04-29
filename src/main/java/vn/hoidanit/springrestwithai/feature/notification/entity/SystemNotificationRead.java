package vn.hoidanit.springrestwithai.feature.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Lưu trạng thái "đã đọc" của từng người dùng đối với các thông báo hệ thống chung.
 * Tương ứng bảng {@code system_notification_read} trên DB.
 */
@Entity
@Table(name = "system_notification_read")
public class SystemNotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "system_notification_id", nullable = false)
    private Long systemNotificationId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "read_at", updatable = false)
    private LocalDateTime readAt;

    @PrePersist
    void prePersist() {
        if (readAt == null) readAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSystemNotificationId() { return systemNotificationId; }
    public void setSystemNotificationId(Long systemNotificationId) { this.systemNotificationId = systemNotificationId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
