package vn.hoidanit.springrestwithai.feature.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Bảng theo dõi những hóa đơn đã được gửi push notification.
 * Mục đích: tránh gửi trùng thông báo nếu Cron Job chạy nhiều lần trong ngày.
 * Nằm ở DB primary (hr_management).
 */
@Entity
@Table(
        name = "notified_invoice",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notified_invoice_id",
                columnNames = {"month_invoice_id"}
        )
)
public class NotifiedInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID của hóa đơn bên bảng monthinvoice (DB qlkh) */
    @Column(name = "month_invoice_id", nullable = false)
    private Integer monthInvoiceId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "invoice_year_month", length = 6)
    private String yearMonth;

    @Column(name = "notified_at", updatable = false)
    private LocalDateTime notifiedAt;

    @PrePersist
    void prePersist() {
        if (notifiedAt == null) notifiedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Integer getMonthInvoiceId() { return monthInvoiceId; }

    public void setMonthInvoiceId(Integer monthInvoiceId) { this.monthInvoiceId = monthInvoiceId; }

    public Integer getCustomerId() { return customerId; }

    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getYearMonth() { return yearMonth; }

    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public LocalDateTime getNotifiedAt() { return notifiedAt; }

    public void setNotifiedAt(LocalDateTime notifiedAt) { this.notifiedAt = notifiedAt; }
}
