package vn.hoidanit.springrestwithai.gv.task.dto;

import jakarta.validation.constraints.NotNull;
import vn.hoidanit.springrestwithai.gv.task.GvTaskStatus;

public record GvTaskStatusUpdateRequest(
    @NotNull(message = "Trạng thái không được để trống")
    GvTaskStatus status,

    String note
) {}
