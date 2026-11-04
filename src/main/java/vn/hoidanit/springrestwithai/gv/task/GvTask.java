package vn.hoidanit.springrestwithai.gv.task;

import java.time.Instant;
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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
import vn.hoidanit.springrestwithai.gv.recurring.GvRecurringTemplate;
import vn.hoidanit.springrestwithai.gv.checklist.GvTaskChecklist;
import vn.hoidanit.springrestwithai.gv.comment.GvTaskComment;
import vn.hoidanit.springrestwithai.gv.attachment.GvTaskAttachment;
import vn.hoidanit.springrestwithai.gv.watcher.GvTaskWatcher;

@Entity
@Table(name = "gv_tasks", indexes = {
    @Index(name = "idx_gv_tasks_status", columnList = "status"),
    @Index(name = "idx_gv_tasks_assignee", columnList = "assignee_id"),
    @Index(name = "idx_gv_tasks_due_date", columnList = "dueDate"),
    @Index(name = "idx_gv_tasks_department", columnList = "department_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GvTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;                    // Mã việc: "TASK-202609-001"

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- Phân công ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;                   // Người giao việc

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;                  // Người phụ trách chính

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private GvDepartment department;         // Phòng ban phụ trách

    // --- Cấu trúc việc cha - con ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GvTask parent;                  // Null nếu là công việc cha/độc lập

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<GvTask> subTasks = new ArrayList<>();

    // --- Nguồn gốc từ việc định kỳ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_template_id")
    private GvRecurringTemplate sourceTemplate; // Null nếu là việc phát sinh

    // --- Trạng thái & Độ ưu tiên ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private GvTaskStatus status = GvTaskStatus.TO_DO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GvTaskPriority priority = GvTaskPriority.NORMAL;

    // --- Tiến độ & Thời hạn ---
    @Builder.Default
    private Integer progress = 0;           // Tiến độ 0 -> 100%

    private Instant startDate;

    @Column(nullable = false)
    private Instant dueDate;

    private Instant completedAt;

    // --- Nghiệm thu / Phê duyệt ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;                // Người duyệt (thường là sếp/người giao)

    private Instant reviewedAt;

    @Column(length = 500)
    private String reviewFeedback;          // Lý do trả lại hoặc nhận xét nghiệm thu

    // --- Đánh giá / Chấm điểm nghiệm thu (KPI Evaluation) ---
    private Integer rating;                 // Số sao đánh giá (1 -> 5)

    private Integer qualityScore;           // Điểm chất lượng (0 -> 100)

    private Integer timelinessScore;        // Điểm tiến độ (0 -> 100)

    private Double overallScore;            // Điểm tổng hợp KPI (0 -> 100)

    @Column(length = 50)
    private String evaluationGrade;         // Xếp loại: EXCELLENT, GOOD, FAIR, POOR

    // --- Danh sách con liên quan ---
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GvTaskChecklist> checklists = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GvTaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GvTaskAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GvTaskWatcher> watchers = new ArrayList<>();

    // --- Timestamps ---
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.status == null) {
            this.status = GvTaskStatus.TO_DO;
        }
        if (this.priority == null) {
            this.priority = GvTaskPriority.NORMAL;
        }
        if (this.progress == null) {
            this.progress = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
