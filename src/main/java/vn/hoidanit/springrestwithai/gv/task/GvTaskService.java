package vn.hoidanit.springrestwithai.gv.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

public interface GvTaskService {

    Page<GvTaskResponse> getTasks(GvTaskFilterRequest filter, Pageable pageable);

    GvTaskKanbanResponse getKanban(Long departmentId, Long assigneeId);

    GvTaskDetailResponse getDetail(Long id);

    GvTaskResponse create(Long creatorId, CreateGvTaskRequest request);

    GvTaskResponse update(Long id, Long currentUserId, UpdateGvTaskRequest request);

    GvTaskResponse updateStatus(Long id, Long currentUserId, GvTaskStatusUpdateRequest request);

    GvTaskResponse submitForReview(Long id, Long currentUserId);

    GvTaskResponse review(Long id, Long reviewerId, GvTaskReviewRequest request);

    void addWatcher(Long taskId, Long userId, Long currentUserId);

    void removeWatcher(Long taskId, Long userId, Long currentUserId);

    GvDashboardStatsResponse getDashboardStats(Long currentUserId);

    /** Thống kê tổng quan toàn công ty cho SuperAdmin */
    GvDashboardSummaryResponse getDashboardSummary();

    void cancel(Long id, Long currentUserId, String reason);
}
