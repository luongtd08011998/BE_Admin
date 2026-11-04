package vn.hoidanit.springrestwithai.gv.task.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import vn.hoidanit.springrestwithai.gv.label.dto.GvLabelResponse;
import vn.hoidanit.springrestwithai.gv.task.GvTaskPriority;
import vn.hoidanit.springrestwithai.gv.task.GvTaskStatus;

@Builder
public record GvTaskResponse(
    Long id,
    String code,
    String title,
    String description,
    Long creatorId,
    String creatorName,
    Long assigneeId,
    String assigneeName,
    String assigneeEmail,
    Long departmentId,
    String departmentName,
    Long parentId,
    String parentTitle,
    GvTaskStatus status,
    GvTaskPriority priority,
    Integer progress,
    Instant startDate,
    Instant dueDate,
    Instant completedAt,
    Long reviewedById,
    String reviewedByName,
    Instant reviewedAt,
    String reviewFeedback,
    Integer rating,
    Integer qualityScore,
    Integer timelinessScore,
    Double overallScore,
    String evaluationGrade,
    Integer subTasksCount,
    Integer checklistsCount,
    Integer completedChecklistsCount,
    List<GvLabelResponse> labels,
    Instant createdAt,
    Instant updatedAt
) {}
