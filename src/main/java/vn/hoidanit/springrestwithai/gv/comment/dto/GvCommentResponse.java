package vn.hoidanit.springrestwithai.gv.comment.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GvCommentResponse(
    Long id,
    Long taskId,
    Long userId,
    String userName,
    String userEmail,
    String content,
    Instant createdAt,
    Instant updatedAt
) {}
