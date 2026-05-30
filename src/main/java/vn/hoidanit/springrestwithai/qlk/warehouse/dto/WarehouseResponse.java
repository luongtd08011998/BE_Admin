package vn.hoidanit.springrestwithai.qlk.warehouse.dto;

import java.time.Instant;
import vn.hoidanit.springrestwithai.qlk.warehouse.Warehouse;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseStatus;

public record WarehouseResponse(
    Long id,
    String code,
    String name,
    String address,
    String description,
    Integer alertThreshold,
    WarehouseStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static WarehouseResponse from(Warehouse warehouse) {
        if (warehouse == null) return null;
        return new WarehouseResponse(
            warehouse.getId(),
            warehouse.getCode(),
            warehouse.getName(),
            warehouse.getAddress(),
            warehouse.getDescription(),
            warehouse.getAlertThreshold() != null ? warehouse.getAlertThreshold() : 10,
            warehouse.getStatus(),
            warehouse.getCreatedAt(),
            warehouse.getUpdatedAt()
        );
    }
}
