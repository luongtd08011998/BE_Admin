package vn.hoidanit.springrestwithai.gv.task;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.gv.auditlog.GvAuditAction;
import vn.hoidanit.springrestwithai.gv.task.dto.CreateGvTaskRequest;
import vn.hoidanit.springrestwithai.gv.task.dto.GvDashboardStatsResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskDetailResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskFilterRequest;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskKanbanResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskResponse;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskReviewRequest;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskStatusUpdateRequest;
import vn.hoidanit.springrestwithai.gv.task.dto.UpdateGvTaskRequest;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;

@RestController
@RequestMapping("/api/v1/gv/tasks")
public class GvTaskController {

    private final GvTaskService taskService;

    public GvTaskController(GvTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GvTaskResponse>>> getTasks(
            GvTaskFilterRequest filter,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasks(filter, pageable)));
    }

    @GetMapping("/kanban")
    public ResponseEntity<ApiResponse<GvTaskKanbanResponse>> getKanban(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long assigneeId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getKanban(departmentId, assigneeId)));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<GvDashboardStatsResponse>> getDashboardStats() {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(taskService.getDashboardStats(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GvTaskDetailResponse>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getDetail(id)));
    }

    @PostMapping
    @GvAuditAction(action = "CREATE_TASK", entity = "GvTask", description = "Tạo công việc mới")
    public ResponseEntity<ApiResponse<GvTaskResponse>> create(@Valid @RequestBody CreateGvTaskRequest request) {
        Long creatorId = GvSecurityUtil.getRequiredCurrentUserId();
        GvTaskResponse response = taskService.create(creatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @GvAuditAction(action = "UPDATE_TASK", entity = "GvTask", description = "Cập nhật thông tin công việc")
    public ResponseEntity<ApiResponse<GvTaskResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGvTaskRequest request) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(taskService.update(id, userId, request)));
    }

    @PatchMapping("/{id}/status")
    @GvAuditAction(action = "UPDATE_TASK_STATUS", entity = "GvTask", description = "Cập nhật trạng thái công việc")
    public ResponseEntity<ApiResponse<GvTaskResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody GvTaskStatusUpdateRequest request) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(taskService.updateStatus(id, userId, request)));
    }

    @PostMapping("/{id}/submit-review")
    @GvAuditAction(action = "SUBMIT_TASK_REVIEW", entity = "GvTask", description = "Nộp việc chờ nghiệm thu")
    public ResponseEntity<ApiResponse<GvTaskResponse>> submitForReview(@PathVariable Long id) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(taskService.submitForReview(id, userId)));
    }

    @PostMapping("/{id}/review")
    @GvAuditAction(action = "REVIEW_TASK", entity = "GvTask", description = "Nghiệm thu hoặc trả lại công việc")
    public ResponseEntity<ApiResponse<GvTaskResponse>> review(
            @PathVariable Long id,
            @Valid @RequestBody GvTaskReviewRequest request) {
        Long reviewerId = GvSecurityUtil.getRequiredCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(taskService.review(id, reviewerId, request)));
    }

    @PostMapping("/{id}/watchers/{userId}")
    @GvAuditAction(action = "ADD_TASK_WATCHER", entity = "GvTask", description = "Thêm người theo dõi")
    public ResponseEntity<ApiResponse<Void>> addWatcher(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Long currentUserId = GvSecurityUtil.getRequiredCurrentUserId();
        taskService.addWatcher(id, userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}/watchers/{userId}")
    @GvAuditAction(action = "REMOVE_TASK_WATCHER", entity = "GvTask", description = "Xóa người theo dõi")
    public ResponseEntity<ApiResponse<Void>> removeWatcher(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Long currentUserId = GvSecurityUtil.getRequiredCurrentUserId();
        taskService.removeWatcher(id, userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/cancel")
    @GvAuditAction(action = "CANCEL_TASK", entity = "GvTask", description = "Hủy bỏ công việc")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Hủy bỏ bởi người dùng") String reason) {
        Long currentUserId = GvSecurityUtil.getRequiredCurrentUserId();
        taskService.cancel(id, currentUserId, reason);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
