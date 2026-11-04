package vn.hoidanit.springrestwithai.gv.task.dto;

import java.time.Instant;
import java.util.List;
import vn.hoidanit.springrestwithai.gv.task.GvTaskPriority;
import vn.hoidanit.springrestwithai.gv.task.GvTaskStatus;

public record GvTaskFilterRequest(
    String keyword,
    GvTaskStatus status,
    List<GvTaskStatus> statuses,
    GvTaskPriority priority,
    Long departmentId,
    Long assigneeId,
    Long creatorId,
    Long parentId,
    Boolean isRoot,
    Boolean isOverdue,
    Instant dueDateFrom,
    Instant dueDateTo
) {}
