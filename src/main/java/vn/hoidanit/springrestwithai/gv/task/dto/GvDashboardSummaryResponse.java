package vn.hoidanit.springrestwithai.gv.task.dto;

import lombok.Builder;

/**
 * DTO phản hồi tổng quan Dashboard toàn công ty cho SuperAdmin.
 * Khớp chính xác với DashboardSummaryData trên App (KMP).
 */
@Builder
public record GvDashboardSummaryResponse(
    long totalTasks,
    long completedTasks,
    long doneTasks,
    long inProgressTasks,
    long pendingReviewTasks,
    long overdueTasks,
    double completionRate,
    int departmentsCount,
    int totalEmployees
) {}
