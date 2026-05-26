package vn.hoidanit.springrestwithai.feature.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Theo dõi mốc ID hóa đơn đã xử lý theo từng ngày — dùng cho InvoiceNotificationScheduler.
 * Mỗi ngày 1 row (datePrefix = "yyyyMMdd").
 */
@Entity
@Table(name = "invoice_sync_tracker", uniqueConstraints = @UniqueConstraint(name = "uk_invoice_sync_date", columnNames = {"date_prefix"}))
public class InvoiceSyncTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_prefix", nullable = false, length = 10)
    private String datePrefix;

    @Column(name = "last_processed_invoice_id", nullable = false)
    private Integer lastProcessedInvoiceId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDatePrefix() { return datePrefix; }
    public void setDatePrefix(String datePrefix) { this.datePrefix = datePrefix; }

    public Integer getLastProcessedInvoiceId() { return lastProcessedInvoiceId; }
    public void setLastProcessedInvoiceId(Integer lastProcessedInvoiceId) { this.lastProcessedInvoiceId = lastProcessedInvoiceId; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
