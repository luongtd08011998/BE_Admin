package vn.hoidanit.springrestwithai.gv.watcher.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GvWatcherResponse(
    Long id,
    Long taskId,
    Long userId,
    String userName,
    String userEmail,
    Long addedById,
    String addedByName,
    Instant addedAt
) {}
