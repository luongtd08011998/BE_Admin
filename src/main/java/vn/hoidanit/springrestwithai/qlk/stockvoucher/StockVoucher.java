package vn.hoidanit.springrestwithai.qlk.stockvoucher;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.qlk.supplier.Supplier;
import vn.hoidanit.springrestwithai.qlk.warehouse.Warehouse;

@Entity
@Table(name = "qlk_stock_vouchers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"voucher_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voucher_code", unique = true, nullable = false, length = 50)
    private String voucherCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherStatus status;

    private LocalDate issuedDate;

    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private Instant approvedAt;

    @Column(length = 500)
    private String rejectReason;

    private Instant createdAt;
    
    private Instant updatedAt;

    @OneToMany(mappedBy = "stockVoucher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VoucherDetail> details = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = VoucherStatus.NHAP_BAN;
        }
        if (this.issuedDate == null) {
            this.issuedDate = LocalDate.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
    
    public void addDetail(VoucherDetail detail) {
        details.add(detail);
        detail.setStockVoucher(this);
    }
    
    public void removeDetail(VoucherDetail detail) {
        details.remove(detail);
        detail.setStockVoucher(null);
    }
}
