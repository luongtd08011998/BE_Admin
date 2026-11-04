package vn.hoidanit.springrestwithai.gv.report.dto;

import lombok.Builder;

@Builder
public record GvEmployeePerformanceDto(
    Long userId,
    String displayName,
    String email,
    String roleTitle,
    Long departmentId,
    String departmentName,
    int totalTasks,
    int completedTasks,
    double completionRate,
    double averageRating,
    double averageKpiScore,
    double averageQualityScore,
    double averageTimelinessScore,
    int excellentCount,
    int goodCount,
    int fairCount,
    int poorCount,
    int inProgressCount,
    int pendingReviewCount,
    int overdueCount,
    int rank,
    // Weighted completion: Bắt buộc (HIGH/URGENT) vs Không bắt buộc (NORMAL/LOW)
    int mandatoryTotalTasks,
    int mandatoryCompletedTasks,
    int optionalTotalTasks,
    int optionalCompletedTasks
) {}
