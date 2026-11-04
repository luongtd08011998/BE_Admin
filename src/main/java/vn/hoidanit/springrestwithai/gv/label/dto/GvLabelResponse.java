package vn.hoidanit.springrestwithai.gv.label.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GvLabelResponse(
    Long id,
    String name,
    String color,
    String description,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {}
