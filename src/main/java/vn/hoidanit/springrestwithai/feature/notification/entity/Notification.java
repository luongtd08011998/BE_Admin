package vn.hoidanit.springrestwithai.feature.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Lưu trữ thông báo gửi tới khách hàng.
 * Tương ứng bảng {@code notification} trên DB primary (hr_management).
 */
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** INVOICE hoặc PAYMENT hoặc FEEDBACK */
    @Column(name = "type", length = 50)
    private String type;

    /**
     * ID tham chiếu đến đối tượng liên quan (feedbackId khi type=FEEDBACK).
     * Dùng để Mobile deep link đến đúng màn hình chi tiết.
     */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isRead == null) isRead = false;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Integer getCustomerId() { return customerId; }

    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public Long getReferenceId() { return referenceId; }

    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public Boolean getIsRead() { return isRead; }

    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
