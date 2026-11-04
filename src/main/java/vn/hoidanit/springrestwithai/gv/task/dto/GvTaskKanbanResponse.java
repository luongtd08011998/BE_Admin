package vn.hoidanit.springrestwithai.gv.task.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record GvTaskKanbanResponse(
    List<GvTaskResponse> todo,
    List<GvTaskResponse> inProgress,
    List<GvTaskResponse> pendingReview,
    List<GvTaskResponse> rejected,
    List<GvTaskResponse> done,
    List<GvTaskResponse> cancelled
) {}
