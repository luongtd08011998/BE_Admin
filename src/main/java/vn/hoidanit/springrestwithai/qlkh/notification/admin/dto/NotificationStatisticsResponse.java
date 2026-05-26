package vn.hoidanit.springrestwithai.qlkh.notification.admin.dto;

import java.util.Map;

public record NotificationStatisticsResponse(
        long totalSent,
        long totalDelivered,
        long totalFailed,
        long totalPending,
        long totalNoDevice,
        long totalPartial,
        Map<String, Long> byType,
        Map<String, Long> byDeliveryStatus,
        long last7Days,
        long last30Days
) {}
