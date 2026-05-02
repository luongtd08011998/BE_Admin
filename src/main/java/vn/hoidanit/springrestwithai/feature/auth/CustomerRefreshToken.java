package vn.hoidanit.springrestwithai.feature.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Lưu Refresh Token của khách hàng QLKH (Mobile).
 * Bảng nằm trong DB chính (hr_management) và được quản lý bởi primaryEntityManagerFactory.
 */
@Entity
@Table(name = "customer_refresh_token")
public class CustomerRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UUID ngẫu nhiên — dùng làm giá trị Refresh Token gửi cho Mobile. */
    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token;

    /** customerId từ bảng customer trên DB QLKH. */
    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    /** Mã khách hàng (DigiCode) — lưu để debug nhanh. */
    @Column(name = "digi_code", length = 50)
    private String digiCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CustomerRefreshToken() {
    }

    public CustomerRefreshToken(String token, Integer customerId, String digiCode,
            Instant expiresAt, Instant createdAt) {
        this.token = token;
        this.customerId = customerId;
        this.digiCode = digiCode;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }

    public String getToken() { return token; }

    public Integer getCustomerId() { return customerId; }

    public String getDigiCode() { return digiCode; }

    public Instant getExpiresAt() { return expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
}
