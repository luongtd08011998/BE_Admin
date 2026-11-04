package vn.hoidanit.springrestwithai.gv.task.dto;

import lombok.Builder;

@Builder
public record GvDashboardStatsResponse(
    long totalTasks,
    long todoTasks,
    long inProgressTasks,
    long pendingReviewTasks,
    long rejectedTasks,
    long doneTasks,
    long cancelledTasks,
    long overdueTasks,
    long myAssignedTasks,
    long myCreatedTasks
) {}
