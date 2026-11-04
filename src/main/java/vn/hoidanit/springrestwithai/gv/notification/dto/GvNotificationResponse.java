package vn.hoidanit.springrestwithai.gv.notification.dto;

import java.time.Instant;
import lombok.Builder;
import vn.hoidanit.springrestwithai.gv.notification.GvNotificationType;

@Builder
public record GvNotificationResponse(
    Long id,
    Long taskId,
    String taskCode,
    String taskTitle,
    GvNotificationType type,
    String title,
    String content,
    Boolean isRead,
    Instant readAt,
    Instant createdAt
) {}
