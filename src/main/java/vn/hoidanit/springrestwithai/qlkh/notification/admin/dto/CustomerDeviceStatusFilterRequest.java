package vn.hoidanit.springrestwithai.qlkh.notification.admin.dto;

public record CustomerDeviceStatusFilterRequest(
        String status,
        String keyword,
        Short isActive,
        Integer roadId
) {}
