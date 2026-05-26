package vn.hoidanit.springrestwithai.qlkh.notification.admin.dto;

import java.time.LocalDateTime;

public record NotificationAdminFilterRequest(
        String type,
        String deliveryStatus,
        Integer customerId,
        LocalDateTime createdFrom,
        LocalDateTime createdTo
) {}
