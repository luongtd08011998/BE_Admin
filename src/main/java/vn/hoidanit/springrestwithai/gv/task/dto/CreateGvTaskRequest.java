package vn.hoidanit.springrestwithai.gv.task.dto;

import java.time.Instant;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.hoidanit.springrestwithai.gv.task.GvTaskPriority;

public record CreateGvTaskRequest(
    @NotBlank(message = "Tiêu đề công việc không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    String title,

    String description,

    @NotNull(message = "Người nhận việc không được để trống")
    Long assigneeId,

    Long departmentId,
    Long parentId,
    GvTaskPriority priority,
    Instant startDate,

    @NotNull(message = "Hạn chót (deadline) không được để trống")
    Instant dueDate,

    List<Long> labelIds,
    List<Long> watcherIds
) {}
