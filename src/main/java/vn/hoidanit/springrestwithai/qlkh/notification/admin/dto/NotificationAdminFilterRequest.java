package vn.hoidanit.springrestwithai.qlkh.notification.admin.dto;

import java.time.LocalDateTime;

public record NotificationAdminFilterRequest(
        String type,
        String deliveryStatus,
        Integer customerId,
        String customerDigiCode,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        Integer roadId
) {}
