package vn.hoidanit.springrestwithai.feature.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Bảng theo dõi mốc ID của paymentline đã xử lý để gửi thông báo.
 * Lưu trữ tại DB Primary (hr_management).
 */
@Entity
@Table(name = "payment_sync_tracker")
public class PaymentSyncTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_processed_payment_line_id", nullable = false)
    private Integer lastProcessedPaymentLineId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLastProcessedPaymentLineId() {
        return lastProcessedPaymentLineId;
    }

    public void setLastProcessedPaymentLineId(Integer lastProcessedPaymentLineId) {
        this.lastProcessedPaymentLineId = lastProcessedPaymentLineId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
