package vn.hoidanit.springrestwithai.gv.checklist;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import vn.hoidanit.springrestwithai.gv.task.GvTask;

@Entity
@Table(name = "gv_task_checklists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GvTaskChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private GvTask task;

    @Column(nullable = false, length = 255)
    private String title;

    @Builder.Default
    private Boolean isCompleted = false;

    @Builder.Default
    @Column(name = "isRequired")
    private Boolean isRequired = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private User completedBy;

    private Instant completedAt;

    @Builder.Default
    private Integer sortOrder = 0;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        if (this.isCompleted == null) {
            this.isCompleted = false;
        }
        if (this.isRequired == null) {
            this.isRequired = true;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }
}
