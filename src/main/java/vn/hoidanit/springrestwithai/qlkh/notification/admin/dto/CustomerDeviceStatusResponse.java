package vn.hoidanit.springrestwithai.qlkh.notification.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerDeviceStatusResponse(
        Integer customerId,
        String digiCode,
        String name,
        String phone,
        String email,
        Short isActive,
        boolean deviceRegistered,
        int deviceCount,
        List<String> platforms,
        LocalDateTime lastRegisteredAt
) {}
