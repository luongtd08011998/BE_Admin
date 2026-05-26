package vn.hoidanit.springrestwithai.qlkh.notification.admin.dto;

import java.time.LocalDateTime;

public record NotificationAdminResponse(
        Long id,
        Integer customerId,
        String customerName,
        String customerDigiCode,
        String title,
        String type,
        Long referenceId,
        Boolean isRead,
        String deliveryStatus,
        LocalDateTime deliveredAt,
        String failureReason,
        LocalDateTime createdAt
) {}
