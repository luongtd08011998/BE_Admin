package vn.hoidanit.springrestwithai.gv.checklist.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GvChecklistResponse(
    Long id,
    Long taskId,
    String title,
    Boolean isCompleted,
    Long completedById,
    String completedByName,
    Instant completedAt,
    Integer sortOrder,
    Boolean isRequired,
    Instant createdAt
) {}
