package vn.hoidanit.springrestwithai.gv.auditlog;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.springrestwithai.feature.user.User;

@Entity
@Table(name = "gv_audit_logs", indexes = {
    @Index(name = "idx_gv_audit_entity", columnList = "entityName, entityId"),
    @Index(name = "idx_gv_audit_user", columnList = "user_id"),
    @Index(name = "idx_gv_audit_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GvAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;                      // Người thực hiện thao tác

    @Column(nullable = false, length = 50)
    private String action;                  // "CREATE_TASK", "CHANGE_STATUS", "APPROVE_TASK", "REJECT_TASK"

    @Column(length = 100)
    private String entityName;              // "GvTask", "GvRecurringTemplate"

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;                 // JSON hoặc text mô tả chi tiết

    @Column(length = 50)
    private String ipAddress;

    private Instant timestamp;

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
