package vn.hoidanit.springrestwithai.gv.recurring.dto;

import java.time.Instant;
import lombok.Builder;
import vn.hoidanit.springrestwithai.gv.recurring.GvFrequencyType;
import vn.hoidanit.springrestwithai.gv.recurring.GvOverdueAction;

@Builder
public record GvRecurringResponse(
    Long id,
    String title,
    String description,
    Long departmentId,
    String departmentName,
    Long creatorId,
    String creatorName,
    Long defaultAssigneeId,
    String defaultAssigneeName,
    GvFrequencyType frequency,
    String cronExpression,
    Integer dayOfWeek,
    Integer dayOfMonth,
    String timeOfDay,
    Integer deadlineDaysOffset,
    GvOverdueAction overdueAction,
    Boolean isActive,
    Instant lastGeneratedAt,
    Instant nextRunAt,
    Instant createdAt,
    Instant updatedAt
) {}
