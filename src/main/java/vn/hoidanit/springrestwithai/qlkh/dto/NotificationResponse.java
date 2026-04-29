package vn.hoidanit.springrestwithai.qlkh.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Integer customerId,
        String title,
        String content,
        String type,
        Boolean isRead,
        LocalDateTime createdAt,
        Long referenceId,
        Boolean isSystem,
        String url
) {}
