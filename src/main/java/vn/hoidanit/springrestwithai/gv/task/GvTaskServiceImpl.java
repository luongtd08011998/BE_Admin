package vn.hoidanit.springrestwithai.gv.task;

import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.exception.BadRequestException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.gv.attachment.GvTaskAttachmentRepository;
import vn.hoidanit.springrestwithai.gv.attachment.dto.GvAttachmentResponse;
import vn.hoidanit.springrestwithai.gv.checklist.GvTaskChecklist;
import vn.hoidanit.springrestwithai.gv.checklist.GvTaskChecklistRepository;
import vn.hoidanit.springrestwithai.gv.checklist.dto.GvChecklistResponse;
import vn.hoidanit.springrestwithai.gv.comment.GvTaskComment;
import vn.hoidanit.springrestwithai.gv.comment.GvTaskCommentRepository;
import vn.hoidanit.springrestwithai.gv.comment.dto.GvCommentResponse;
import vn.hoidanit.springrestwithai.gv.department.GvDepartment;
import vn.hoidanit.springrestwithai.gv.department.GvDepartmentRepository;
import vn.hoidanit.springrestwithai.gv.label.GvLabel;
import vn.hoidanit.springrestwithai.gv.label.GvLabelRepository;
import vn.hoidanit.springrestwithai.gv.label.GvTaskLabel;
import vn.hoidanit.springrestwithai.gv.label.GvTaskLabelRepository;
import vn.hoidanit.springrestwithai.gv.label.dto.GvLabelResponse;
import vn.hoidanit.springrestwithai.gv.notification.GvNotificationService;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;
import vn.hoidanit.springrestwithai.gv.notification.GvNotificationType;
import vn.hoidanit.springrestwithai.gv.task.dto.CreateGvTaskRequest;
import vn.hoidanit.springrestwithai.gv.task.dto.GvDashboardStatsResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvDashboardSummaryResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskDetailResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskFilterRequest;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskKanbanResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskReviewRequest;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskStatusUpdateRequest;
import vn.hoidanit.springrestwithai.gv.task.dto.UpdateGvTaskRequest;
import vn.hoidanit.springrestwithai.gv.watcher.GvTaskWatcher;
import vn.hoidanit.springrestwithai.gv.watcher.GvTaskWatcherRepository;
import vn.hoidanit.springrestwithai.gv.watcher.dto.GvWatcherResponse;

@Service
@Transactional
public class GvTaskServiceImpl implements GvTaskService {

    private final GvTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GvDepartmentRepository departmentRepository;
    private final GvTaskWatcherRepository watcherRepository;
    private final GvTaskChecklistRepository checklistRepository;
    private final GvTaskCommentRepository commentRepository;
    private final GvTaskAttachmentRepository attachmentRepository;
    private final GvLabelRepository labelRepository;
    private final GvTaskLabelRepository taskLabelRepository;
    private final GvNotificationService notificationService;

    public GvTaskServiceImpl(
            GvTaskRepository taskRepository,
            UserRepository userRepository,
            GvDepartmentRepository departmentRepository,
            GvTaskWatcherRepository watcherRepository,
            GvTaskChecklistRepository checklistRepository,
            GvTaskCommentRepository commentRepository,
            GvTaskAttachmentRepository attachmentRepository,
            GvLabelRepository labelRepository,
            GvTaskLabelRepository taskLabelRepository,
            GvNotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.watcherRepository = watcherRepository;
        this.checklistRepository = checklistRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
        this.labelRepository = labelRepository;
        this.taskLabelRepository = taskLabelRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GvTaskResponse> getTasks(GvTaskFilterRequest filter, Pageable pageable) {
        Long currentUserId = GvSecurityUtil.getCurrentUserId();
        boolean isSuperAdmin = GvSecurityUtil.isSuperAdmin();

        // Phân quyền theo cấp bậc: SUPER_ADMIN / ADMIN -> MANAGER -> USER
        if (!isSuperAdmin && currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            boolean isManager = currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().stream()
                    .anyMatch(r -> {
                        String name = r.getName().toUpperCase();
                        return name.contains("MANAGER") || name.contains("TRUONG_PHONG") || name.contains("GIAM_DOC");
                    });

            Long managerDeptId = null;
            if (isManager && currentUser.getDepartment() != null) {
                managerDeptId = currentUser.getDepartment().getId();
            }

            if (isManager && managerDeptId != null) {
                // MANAGER: Mặc định xem toàn bộ task trong phòng ban mình
                Long deptId = (filter != null && filter.departmentId() != null) ? filter.departmentId() : managerDeptId;
                if (filter == null) {
                    filter = new GvTaskFilterRequest(
                            null, null, null, null, deptId, null, null, null, null, null, null, null
                    );
                } else {
                    filter = new GvTaskFilterRequest(
                            filter.keyword(),
                            filter.status(),
                            filter.statuses(),
                            filter.priority(),
                            deptId,
                            filter.assigneeId(),
                            filter.creatorId(),
                            filter.parentId(),
                            filter.isRoot(),
                            filter.isOverdue(),
                            filter.dueDateFrom(),
                            filter.dueDateTo()
                    );
                }
            } else {
                // USER bình thường: Mặc định chỉ xem công việc của chính mình
                if (filter == null) {
                    filter = new GvTaskFilterRequest(
                            null, null, null, null, null, currentUserId, null, null, null, null, null, null
                    );
                } else if (filter.assigneeId() == null && filter.creatorId() == null) {
                    filter = new GvTaskFilterRequest(
                            filter.keyword(),
                            filter.status(),
                            filter.statuses(),
                            filter.priority(),
                            filter.departmentId(),
                            currentUserId,
                            filter.creatorId(),
                            filter.parentId(),
                            filter.isRoot(),
                            filter.isOverdue(),
                            filter.dueDateFrom(),
                            filter.dueDateTo()
                    );
                }
            }
        }

        return taskRepository.findAll(GvTaskSpecification.build(filter), pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GvTaskKanbanResponse getKanban(Long departmentId, Long assigneeId) {
        Long currentUserId = GvSecurityUtil.getCurrentUserId();
        boolean isSuperAdmin = GvSecurityUtil.isSuperAdmin();

        if (!isSuperAdmin && currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            boolean isManager = currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().stream()
                    .anyMatch(r -> {
                        String name = r.getName().toUpperCase();
                        return name.contains("MANAGER") || name.contains("TRUONG_PHONG") || name.contains("GIAM_DOC");
                    });

            if (isManager && currentUser.getDepartment() != null) {
                // Manager mặc định xem kanban cả phòng ban của mình
                if (departmentId == null) {
                    departmentId = currentUser.getDepartment().getId();
                }
            } else {
                // User bình thường chỉ xem kanban của chính mình
                if (assigneeId == null) {
                    assigneeId = currentUserId;
                }
            }
        }

        GvTaskFilterRequest filter = new GvTaskFilterRequest(
                null, null, null, null, departmentId, assigneeId, null, null, true, null, null, null
        );

        List<GvTask> allTasks = taskRepository.findAll(GvTaskSpecification.build(filter));

        List<GvTaskResponse> todo = new ArrayList<>();
        List<GvTaskResponse> inProgress = new ArrayList<>();
        List<GvTaskResponse> pendingReview = new ArrayList<>();
        List<GvTaskResponse> rejected = new ArrayList<>();
        List<GvTaskResponse> done = new ArrayList<>();
        List<GvTaskResponse> cancelled = new ArrayList<>();

        for (GvTask t : allTasks) {
            GvTaskResponse resp = toResponse(t);
            switch (t.getStatus()) {
                case TO_DO -> todo.add(resp);
                case IN_PROGRESS -> inProgress.add(resp);
                case PENDING_REVIEW -> pendingReview.add(resp);
                case REJECTED -> rejected.add(resp);
                case DONE -> done.add(resp);
                case CANCELLED -> cancelled.add(resp);
            }
        }

        return GvTaskKanbanResponse.builder()
                .todo(todo)
                .inProgress(inProgress)
                .pendingReview(pendingReview)
                .rejected(rejected)
                .done(done)
                .cancelled(cancelled)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GvTaskDetailResponse getDetail(Long id) {
        GvTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        GvTaskResponse taskResp = toResponse(task);

        List<GvTaskResponse> subTasks = taskRepository.findByParentId(id).stream()
                .map(this::toResponse)
                .toList();

        List<GvChecklistResponse> checklists = checklistRepository.findByTaskIdOrderBySortOrderAsc(id).stream()
                .map(c -> GvChecklistResponse.builder()
                        .id(c.getId())
                        .taskId(id)
                        .title(c.getTitle())
                        .isCompleted(c.getIsCompleted())
                        .completedById(c.getCompletedBy() != null ? c.getCompletedBy().getId() : null)
                        .completedByName(c.getCompletedBy() != null ? c.getCompletedBy().getName() : null)
                        .completedAt(c.getCompletedAt())
                        .sortOrder(c.getSortOrder())
                        .isRequired(c.getIsRequired() != null ? c.getIsRequired() : true)
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        List<GvCommentResponse> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(id).stream()
                .map(c -> GvCommentResponse.builder()
                        .id(c.getId())
                        .taskId(id)
                        .userId(c.getUser().getId())
                        .userName(c.getUser().getName())
                        .userEmail(c.getUser().getEmail())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .updatedAt(c.getUpdatedAt())
                        .build())
                .toList();

        List<GvAttachmentResponse> attachments = attachmentRepository.findByTaskId(id).stream()
                .map(a -> GvAttachmentResponse.builder()
                        .id(a.getId())
                        .taskId(id)
                        .commentId(a.getComment() != null ? a.getComment().getId() : null)
                        .fileName(a.getFileName())
                        .fileUrl(a.getFileUrl())
                        .fileSize(a.getFileSize())
                        .fileType(a.getFileType())
                        .uploadedById(a.getUploadedBy().getId())
                        .uploadedByName(a.getUploadedBy().getName())
                        .uploadedAt(a.getUploadedAt())
                        .build())
                .toList();

        List<GvWatcherResponse> watchers = watcherRepository.findByTaskId(id).stream()
                .map(w -> GvWatcherResponse.builder()
                        .id(w.getId())
                        .taskId(id)
                        .userId(w.getUser().getId())
                        .userName(w.getUser().getName())
                        .userEmail(w.getUser().getEmail())
                        .addedById(w.getAddedBy() != null ? w.getAddedBy().getId() : null)
                        .addedByName(w.getAddedBy() != null ? w.getAddedBy().getName() : null)
                        .addedAt(w.getAddedAt())
                        .build())
                .toList();

        return GvTaskDetailResponse.builder()
                .task(taskResp)
                .subTasks(subTasks)
                .checklists(checklists)
                .comments(comments)
                .attachments(attachments)
                .watchers(watchers)
                .build();
    }

    @Override
    public GvTaskResponse create(Long creatorId, CreateGvTaskRequest request) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng giao việc", "id", creatorId));

        User assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Người nhận việc", "id", request.assigneeId()));

        boolean isSuperOrAdmin = GvSecurityUtil.isSuperAdmin();
        Long creatorDeptId = creator.getDepartment() != null ? creator.getDepartment().getId() : null;
        if (creatorDeptId == null) {
            // Kiểm tra xem creator có phải là manager của phòng ban nào không
            List<GvDepartment> managedDepts = departmentRepository.findByIsActiveTrue().stream()
                    .filter(d -> d.getManager() != null && d.getManager().getId().equals(creatorId))
                    .toList();
            if (!managedDepts.isEmpty()) {
                creatorDeptId = managedDepts.get(0).getId();
            }
        }

        // Nếu không phải SuperAdmin/Admin: bắt buộc kiểm tra phạm vi phòng ban của Manager
        if (!isSuperOrAdmin) {
            boolean isManager = creator.getRoles() != null && creator.getRoles().stream()
                    .anyMatch(r -> {
                        String name = r.getName().toUpperCase();
                        return name.contains("MANAGER") || name.contains("TRUONG_PHONG") || name.contains("GIAM_DOC");
                    });

            if (isManager && creatorDeptId != null) {
                // 1. Trưởng phòng chỉ được giao việc cho phòng ban của mình
                if (request.departmentId() != null && !request.departmentId().equals(creatorDeptId)) {
                    throw new org.springframework.security.access.AccessDeniedException(
                            "Trưởng phòng chỉ được quyền giao việc cho phòng ban của mình phụ trách");
                }
                // 2. Nhân sự nhận việc phải thuộc phòng ban của Manager
                if (assignee.getDepartment() != null && !assignee.getDepartment().getId().equals(creatorDeptId)) {
                    throw new org.springframework.security.access.AccessDeniedException(
                            "Không thể giao việc cho nhân sự thuộc phòng ban khác");
                }
            }
        }

        GvDepartment department = null;
        Long targetDeptId = request.departmentId() != null ? request.departmentId() : creatorDeptId;
        if (targetDeptId != null) {
            department = departmentRepository.findById(targetDeptId)
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", targetDeptId));
        }

        GvTask parent = null;
        if (request.parentId() != null) {
            parent = taskRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Việc cha", "id", request.parentId()));
        }

        String taskCode = generateTaskCode();

        GvTask task = GvTask.builder()
                .code(taskCode)
                .title(request.title().trim())
                .description(request.description())
                .creator(creator)
                .assignee(assignee)
                .department(department)
                .parent(parent)
                .status(GvTaskStatus.TO_DO)
                .priority(request.priority() != null ? request.priority() : GvTaskPriority.NORMAL)
                .startDate(request.startDate() != null ? request.startDate() : Instant.now())
                .dueDate(request.dueDate())
                .progress(0)
                .build();

        GvTask savedTask = taskRepository.save(task);

        // Gán Labels nếu có
        if (request.labelIds() != null && !request.labelIds().isEmpty()) {
            for (Long labelId : request.labelIds()) {
                labelRepository.findById(labelId).ifPresent(lbl -> {
                    taskLabelRepository.save(GvTaskLabel.builder().task(savedTask).label(lbl).build());
                });
            }
        }

        // Gán Watchers
        if (request.watcherIds() != null && !request.watcherIds().isEmpty()) {
            for (Long watcherId : request.watcherIds()) {
                if (!watcherId.equals(assignee.getId()) && !watcherId.equals(creator.getId())) {
                    userRepository.findById(watcherId).ifPresent(u -> {
                        watcherRepository.save(GvTaskWatcher.builder().task(savedTask).user(u).addedBy(creator).build());
                    });
                }
            }
        }

        // Tự động gán Trưởng phòng ban làm Watcher nếu sếp phòng ban khác người giao/nhận
        if (department != null && department.getManager() != null) {
            Long managerId = department.getManager().getId();
            if (!managerId.equals(assignee.getId()) && !managerId.equals(creator.getId())) {
                if (!watcherRepository.existsByTaskIdAndUserId(savedTask.getId(), managerId)) {
                    watcherRepository.save(GvTaskWatcher.builder()
                            .task(savedTask)
                            .user(department.getManager())
                            .addedBy(creator)
                            .build());
                }
            }
        }

        // Bắn thông báo cho người nhận việc
        if (!assignee.getId().equals(creator.getId())) {
            notificationService.sendNotification(
                    assignee.getId(),
                    savedTask.getId(),
                    GvNotificationType.TASK_ASSIGNED,
                    "Bạn được giao công việc mới: " + savedTask.getCode(),
                    creator.getName() + " đã giao việc: " + savedTask.getTitle()
            );
        }

        return toResponse(savedTask);
    }

    @Override
    public GvTaskResponse update(Long id, Long currentUserId, UpdateGvTaskRequest request) {
        GvTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        if (request.assigneeId() != null && !request.assigneeId().equals(task.getAssignee().getId())) {
            User newAssignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người nhận việc", "id", request.assigneeId()));
            task.setAssignee(newAssignee);
        }

        if (request.departmentId() != null) {
            GvDepartment department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", request.departmentId()));
            task.setDepartment(department);
        }

        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.startDate() != null) task.setStartDate(request.startDate());
        task.setDueDate(request.dueDate());
        if (request.progress() != null) task.setProgress(Math.min(100, Math.max(0, request.progress())));

        return toResponse(taskRepository.save(task));
    }

    @Override
    public GvTaskResponse updateStatus(Long id, Long currentUserId, GvTaskStatusUpdateRequest request) {
        GvTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        GvTaskStatus oldStatus = task.getStatus();
        GvTaskStatus newStatus = request.status();

        task.setStatus(newStatus);
        if (newStatus == GvTaskStatus.DONE) {
            task.setCompletedAt(Instant.now());
            task.setProgress(100);
        }

        GvTask saved = taskRepository.save(task);

        // Ghi comment hệ thống nếu có ghi chú
        if (request.note() != null && !request.note().isBlank()) {
            User user = userRepository.findById(currentUserId).orElse(null);
            if (user != null) {
                commentRepository.save(GvTaskComment.builder()
                        .task(saved)
                        .user(user)
                        .content("Đổi trạng thái từ " + oldStatus + " sang " + newStatus + ": " + request.note().trim())
                        .build());
            }
        }

        return toResponse(saved);
    }

    @Override
    public GvTaskResponse submitForReview(Long id, Long currentUserId) {
        GvTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        // Kiểm tra các đầu việc bắt buộc (isRequired = true) chưa hoàn thành
        List<GvTaskChecklist> checklists = checklistRepository.findByTaskIdOrderBySortOrderAsc(id);
        long uncompletedRequired = checklists.stream()
                .filter(c -> (c.getIsRequired() == null || Boolean.TRUE.equals(c.getIsRequired()))
                        && !Boolean.TRUE.equals(c.getIsCompleted()))
                .count();
        if (uncompletedRequired > 0) {
            throw new BadRequestException("Không thể nộp việc! Còn " + uncompletedRequired + " đầu việc bắt buộc chưa hoàn thành.");
        }

        task.setStatus(GvTaskStatus.PENDING_REVIEW);
        GvTask saved = taskRepository.save(task);

        // Bắn thông báo cho người giao việc để duyệt
        User sender = userRepository.findById(currentUserId).orElse(null);
        String senderName = sender != null ? sender.getName() : "Nhân viên";

        notificationService.sendNotification(
                task.getCreator().getId(),
                task.getId(),
                GvNotificationType.REVIEW_SUBMITTED,
                "Yêu cầu nghiệm thu công việc: " + task.getCode(),
                senderName + " đã nộp công việc '" + task.getTitle() + "' chờ bạn duyệt hoàn thành."
        );

        return toResponse(saved);
    }

    @Override
    public GvTaskResponse review(Long id, Long reviewerId, GvTaskReviewRequest request) {
        GvTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Người duyệt", "id", reviewerId));

        boolean isSuperOrAdmin = GvSecurityUtil.isSuperAdmin();
        boolean isCreator = task.getCreator() != null && task.getCreator().getId().equals(reviewerId);
        boolean isDepartmentManager = false;

        if (task.getDepartment() != null) {
            if (task.getDepartment().getManager() != null && task.getDepartment().getManager().getId().equals(reviewerId)) {
                isDepartmentManager = true;
            } else if (reviewer.getDepartment() != null && reviewer.getDepartment().getId().equals(task.getDepartment().getId())) {
                boolean hasManagerRole = reviewer.getRoles() != null && reviewer.getRoles().stream()
                        .anyMatch(r -> {
                            String name = r.getName().toUpperCase();
                            return name.contains("MANAGER") || name.contains("TRUONG_PHONG") || name.contains("GIAM_DOC");
                        });
                if (hasManagerRole) {
                    isDepartmentManager = true;
                }
            }
        }

        if (!isSuperOrAdmin && !isCreator && !isDepartmentManager) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn không có quyền nghiệm thu và đánh giá công việc này (Chỉ Trưởng phòng ban hoặc Admin mới có quyền)");
        }

        // Không cho phép tự duyệt công việc của chính mình (trừ khi là SuperAdmin)
        if (!isSuperOrAdmin && task.getAssignee() != null && task.getAssignee().getId().equals(reviewerId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Người nhận việc không được tự nghiệm thu và đánh giá công việc của chính mình");
        }

        task.setReviewedBy(reviewer);
        task.setReviewedAt(Instant.now());
        task.setReviewFeedback(request.feedback());

        if (Boolean.TRUE.equals(request.approved())) {
            task.setStatus(GvTaskStatus.DONE);
            task.setCompletedAt(Instant.now());
            task.setProgress(100);

            // Chấm điểm và xếp loại đánh giá
            int rating = request.rating() != null ? Math.max(1, Math.min(5, request.rating())) : 5;
            int quality = request.qualityScore() != null ? Math.max(0, Math.min(100, request.qualityScore())) : (rating * 20);

            int timeliness = 100;
            if (request.timelinessScore() != null) {
                timeliness = Math.max(0, Math.min(100, request.timelinessScore()));
            } else if (task.getDueDate() != null && Instant.now().isAfter(task.getDueDate())) {
                long overdueHours = java.time.Duration.between(task.getDueDate(), Instant.now()).toHours();
                if (overdueHours <= 12) timeliness = 85; // Trễ dưới 12h: 85 điểm
                else if (overdueHours <= 24) timeliness = 70; // Trễ 12h - 24h: 70 điểm
                else if (overdueHours <= 48) timeliness = 40; // Trễ 1 - 2 ngày: 40 điểm
                else if (overdueHours <= 72) timeliness = 20; // Trễ 2 - 3 ngày: 20 điểm
                else timeliness = 0; // Trễ quá 3 ngày: 0 điểm
            }

            double overall = Math.round(((quality * 0.7) + (timeliness * 0.3)) * 10.0) / 10.0;
            String grade;
            if (overall >= 90) grade = "EXCELLENT";
            else if (overall >= 75) grade = "GOOD";
            else if (overall >= 60) grade = "FAIR";
            else grade = "POOR";

            task.setRating(rating);
            task.setQualityScore(quality);
            task.setTimelinessScore(timeliness);
            task.setOverallScore(overall);
            task.setEvaluationGrade(grade);

            notificationService.sendNotification(
                    task.getAssignee().getId(),
                    task.getId(),
                    GvNotificationType.REVIEW_APPROVED,
                    "Công việc đã được nghiệm thu & đánh giá: " + task.getCode(),
                    reviewer.getName() + " đã nghiệm thu đạt (" + rating + "⭐ - " + grade + "): " + task.getTitle()
            );
        } else {
            task.setStatus(GvTaskStatus.REJECTED);
            task.setRating(null);
            task.setQualityScore(null);
            task.setTimelinessScore(null);
            task.setOverallScore(null);
            task.setEvaluationGrade(null);

            notificationService.sendNotification(
                    task.getAssignee().getId(),
                    task.getId(),
                    GvNotificationType.REVIEW_REJECTED,
                    "Công việc bị từ chối / cần làm lại: " + task.getCode(),
                    reviewer.getName() + " trả lại công việc với phản hồi: " + (request.feedback() != null ? request.feedback() : "Chưa đạt yêu cầu")
            );
        }

        return toResponse(taskRepository.save(task));
    }

    @Override
    public void addWatcher(Long taskId, Long userId, Long currentUserId) {
        GvTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId));

        User addedBy = userRepository.findById(currentUserId).orElse(null);

        if (!watcherRepository.existsByTaskIdAndUserId(taskId, userId)) {
            watcherRepository.save(GvTaskWatcher.builder()
                    .task(task)
                    .user(user)
                    .addedBy(addedBy)
                    .build());
        }
    }

    @Override
    public void removeWatcher(Long taskId, Long userId, Long currentUserId) {
        watcherRepository.deleteByTaskIdAndUserId(taskId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public GvDashboardStatsResponse getDashboardStats(Long currentUserId) {
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        boolean isManager = currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(r -> {
                    String name = r.getName().toUpperCase();
                    return name.contains("MANAGER") || name.contains("TRUONG_PHONG") || name.contains("GIAM_DOC");
                });
        Long managerDeptId = (isManager && currentUser.getDepartment() != null) ? currentUser.getDepartment().getId() : null;

        long myAssignedTasks = currentUserId != null ? taskRepository.countByAssigneeId(currentUserId) : 0;
        long myCreatedTasks = currentUserId != null ? taskRepository.countByCreatorId(currentUserId) : 0;

        if (isManager && managerDeptId != null) {
            // Thống kê cấp phòng ban cho Trưởng Phòng (Manager)
            long totalTasks = taskRepository.countByDepartmentId(managerDeptId);
            long todoTasks = taskRepository.countByDepartmentIdAndStatus(managerDeptId, GvTaskStatus.TO_DO);
            long inProgressTasks = taskRepository.countByDepartmentIdAndStatus(managerDeptId, GvTaskStatus.IN_PROGRESS);
            long pendingReviewTasks = taskRepository.countByDepartmentIdAndStatus(managerDeptId, GvTaskStatus.PENDING_REVIEW);
            long rejectedTasks = taskRepository.countByDepartmentIdAndStatus(managerDeptId, GvTaskStatus.REJECTED);
            long doneTasks = taskRepository.countByDepartmentIdAndStatus(managerDeptId, GvTaskStatus.DONE);
            long cancelledTasks = taskRepository.countByDepartmentIdAndStatus(managerDeptId, GvTaskStatus.CANCELLED);
            long overdueTasks = taskRepository.countByDepartmentIdAndDueDateBeforeAndStatusNotIn(
                    managerDeptId,
                    Instant.now(),
                    List.of(GvTaskStatus.DONE, GvTaskStatus.CANCELLED)
            );

            return GvDashboardStatsResponse.builder()
                    .totalTasks(totalTasks)
                    .todoTasks(todoTasks)
                    .inProgressTasks(inProgressTasks)
                    .pendingReviewTasks(pendingReviewTasks)
                    .rejectedTasks(rejectedTasks)
                    .doneTasks(doneTasks)
                    .cancelledTasks(cancelledTasks)
                    .overdueTasks(overdueTasks)
                    .myAssignedTasks(myAssignedTasks)
                    .myCreatedTasks(myCreatedTasks)
                    .build();
        }

        // Nhân viên bình thường (USER/EMPLOYEE)
        long totalTasks = myAssignedTasks;
        long todoTasks = currentUserId != null ? taskRepository.countByAssigneeIdAndStatus(currentUserId, GvTaskStatus.TO_DO) : 0;
        long inProgressTasks = currentUserId != null ? taskRepository.countByAssigneeIdAndStatus(currentUserId, GvTaskStatus.IN_PROGRESS) : 0;
        long pendingReviewTasks = currentUserId != null ? taskRepository.countByAssigneeIdAndStatus(currentUserId, GvTaskStatus.PENDING_REVIEW) : 0;
        long rejectedTasks = currentUserId != null ? taskRepository.countByAssigneeIdAndStatus(currentUserId, GvTaskStatus.REJECTED) : 0;
        long doneTasks = currentUserId != null ? taskRepository.countByAssigneeIdAndStatus(currentUserId, GvTaskStatus.DONE) : 0;
        long cancelledTasks = currentUserId != null ? taskRepository.countByAssigneeIdAndStatus(currentUserId, GvTaskStatus.CANCELLED) : 0;
        long overdueTasks = currentUserId != null ? taskRepository.countByAssigneeIdAndDueDateBeforeAndStatusNotIn(
                currentUserId,
                Instant.now(),
                List.of(GvTaskStatus.DONE, GvTaskStatus.CANCELLED)
        ) : 0;

        return GvDashboardStatsResponse.builder()
                .totalTasks(totalTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .pendingReviewTasks(pendingReviewTasks)
                .rejectedTasks(rejectedTasks)
                .doneTasks(doneTasks)
                .cancelledTasks(cancelledTasks)
                .overdueTasks(overdueTasks)
                .myAssignedTasks(myAssignedTasks)
                .myCreatedTasks(myCreatedTasks)
                .build();
    }

    @Override
    public GvDashboardSummaryResponse getDashboardSummary() {
        long totalTasks = taskRepository.count();
        long doneTasks = taskRepository.countByStatus(GvTaskStatus.DONE);
        long inProgressTasks = taskRepository.countByStatus(GvTaskStatus.IN_PROGRESS);
        long pendingReviewTasks = taskRepository.countByStatus(GvTaskStatus.PENDING_REVIEW);

        long overdueTasks = taskRepository.countByDueDateBeforeAndStatusNotIn(
                java.time.Instant.now(),
                java.util.List.of(GvTaskStatus.DONE, GvTaskStatus.CANCELLED)
        );

        double completionRate = totalTasks > 0
                ? (double) doneTasks / totalTasks * 100.0
                : 0.0;

        int departmentsCount = (int) departmentRepository.count();
        int totalEmployees = (int) userRepository.count();

        return GvDashboardSummaryResponse.builder()
                .totalTasks(totalTasks)
                .completedTasks(doneTasks)
                .doneTasks(doneTasks)
                .inProgressTasks(inProgressTasks)
                .pendingReviewTasks(pendingReviewTasks)
                .overdueTasks(overdueTasks)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .departmentsCount(departmentsCount)
                .totalEmployees(totalEmployees)
                .build();
    }

    @Override
    public void cancel(Long id, Long currentUserId, String reason) {
        GvTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", id));

        task.setStatus(GvTaskStatus.CANCELLED);
        task.setReviewFeedback(reason);
        taskRepository.save(task);
    }

    private String generateTaskCode() {
        String ym = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = taskRepository.count() + 1;
        return String.format("TASK-%s-%04d", ym, count);
    }

    private GvTaskResponse toResponse(GvTask t) {
        List<GvLabelResponse> labelResponses = taskLabelRepository.findByTaskId(t.getId()).stream()
                .map(tl -> {
                    GvLabel l = tl.getLabel();
                    return GvLabelResponse.builder()
                            .id(l.getId())
                            .name(l.getName())
                            .color(l.getColor())
                            .description(l.getDescription())
                            .isActive(l.getIsActive())
                            .build();
                })
                .toList();

        long checklistsCount = checklistRepository.countByTaskId(t.getId());
        long completedChecklistsCount = checklistRepository.countByTaskIdAndIsCompletedTrue(t.getId());
        int subTasksCount = taskRepository.findByParentId(t.getId()).size();

        return GvTaskResponse.builder()
                .id(t.getId())
                .code(t.getCode())
                .title(t.getTitle())
                .description(t.getDescription())
                .creatorId(t.getCreator().getId())
                .creatorName(t.getCreator().getName())
                .assigneeId(t.getAssignee().getId())
                .assigneeName(t.getAssignee().getName())
                .assigneeEmail(t.getAssignee().getEmail())
                .departmentId(t.getDepartment() != null ? t.getDepartment().getId() : null)
                .departmentName(t.getDepartment() != null ? t.getDepartment().getName() : null)
                .parentId(t.getParent() != null ? t.getParent().getId() : null)
                .parentTitle(t.getParent() != null ? t.getParent().getTitle() : null)
                .status(t.getStatus())
                .priority(t.getPriority())
                .progress(t.getProgress())
                .startDate(t.getStartDate())
                .dueDate(t.getDueDate())
                .completedAt(t.getCompletedAt())
                .reviewedById(t.getReviewedBy() != null ? t.getReviewedBy().getId() : null)
                .reviewedByName(t.getReviewedBy() != null ? t.getReviewedBy().getName() : null)
                .reviewedAt(t.getReviewedAt())
                .reviewFeedback(t.getReviewFeedback())
                .rating(t.getRating())
                .qualityScore(t.getQualityScore())
                .timelinessScore(t.getTimelinessScore())
                .overallScore(t.getOverallScore())
                .evaluationGrade(t.getEvaluationGrade())
                .subTasksCount(subTasksCount)
                .checklistsCount((int) checklistsCount)
                .completedChecklistsCount((int) completedChecklistsCount)
                .labels(labelResponses)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
