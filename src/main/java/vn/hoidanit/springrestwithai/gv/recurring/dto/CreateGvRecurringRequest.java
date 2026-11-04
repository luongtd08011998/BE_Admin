package vn.hoidanit.springrestwithai.gv.recurring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.hoidanit.springrestwithai.gv.recurring.GvFrequencyType;
import vn.hoidanit.springrestwithai.gv.recurring.GvOverdueAction;

public record CreateGvRecurringRequest(
    @NotBlank(message = "Tiêu đề việc định kỳ không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    String title,

    String description,
    Long departmentId,

    @NotNull(message = "Người nhận việc mặc định không được để trống")
    Long defaultAssigneeId,

    @NotNull(message = "Chu kỳ lặp lại không được để trống")
    GvFrequencyType frequency,

    String cronExpression,
    Integer dayOfWeek,
    Integer dayOfMonth,
    String timeOfDay,
    Integer deadlineDaysOffset,
    GvOverdueAction overdueAction
) {}
