package vn.hoidanit.springrestwithai.qlk.inventory.dto;

import vn.hoidanit.springrestwithai.qlk.inventory.InventorySnapshot;
import vn.hoidanit.springrestwithai.qlk.material.dto.MaterialResponse;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.WarehouseResponse;

public record InventorySnapshotResponse(
    Long id,
    WarehouseResponse warehouse,
    String period,
    MaterialResponse material,
    Integer openingQuantity,
    Integer inboundSum,
    Integer outboundSum,
    Integer closingQuantity
) {
    public static InventorySnapshotResponse from(InventorySnapshot snapshot) {
        if (snapshot == null) return null;
        return new InventorySnapshotResponse(
            snapshot.getId(),
            snapshot.getWarehouse() != null ? WarehouseResponse.from(snapshot.getWarehouse()) : null,
            snapshot.getPeriod(),
            snapshot.getMaterial() != null ? MaterialResponse.from(snapshot.getMaterial()) : null,
            snapshot.getOpeningQuantity(),
            snapshot.getInboundSum(),
            snapshot.getOutboundSum(),
            snapshot.getClosingQuantity()
        );
    }
}
