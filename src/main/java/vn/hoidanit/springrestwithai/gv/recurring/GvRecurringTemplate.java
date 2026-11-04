package vn.hoidanit.springrestwithai.gv.recurring;

import java.time.Instant;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.gv.department.GvDepartment;

@Entity
@Table(name = "gv_recurring_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GvRecurringTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private GvDepartment department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_assignee_id", nullable = false)
    private User defaultAssignee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GvFrequencyType frequency;      // DAILY, WEEKLY, MONTHLY, QUARTERLY

    @Column(length = 50)
    private String cronExpression;          // Cấu hình cron tùy chọn nếu cần

    private Integer dayOfWeek;              // 1 = Monday ... 7 = Sunday (khi frequency = WEEKLY)
    private Integer dayOfMonth;             // 1 ... 31 (khi frequency = MONTHLY)

    @Column(length = 10)
    private String timeOfDay;               // "08:00", "09:30"

    private Integer deadlineDaysOffset;     // Hạn chót = ngày sinh + X ngày

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private GvOverdueAction overdueAction = GvOverdueAction.ALERT_AND_CREATE;

    @Builder.Default
    private Boolean isActive = true;

    private Instant lastGeneratedAt;
    private Instant nextRunAt;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.overdueAction == null) {
            this.overdueAction = GvOverdueAction.ALERT_AND_CREATE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
