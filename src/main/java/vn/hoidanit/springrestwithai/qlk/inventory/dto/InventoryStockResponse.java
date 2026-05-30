package vn.hoidanit.springrestwithai.qlk.inventory.dto;

import java.time.Instant;
import vn.hoidanit.springrestwithai.qlk.inventory.InventoryStock;
import vn.hoidanit.springrestwithai.qlk.material.dto.MaterialResponse;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.WarehouseResponse;

public record InventoryStockResponse(
    Long id,
    WarehouseResponse warehouse,
    MaterialResponse material,
    Integer quantity,
    Integer reservedQuantity,
    Instant lastUpdated
) {
    public static InventoryStockResponse from(InventoryStock stock) {
        if (stock == null) return null;
        return new InventoryStockResponse(
            stock.getId(),
            stock.getWarehouse() != null ? WarehouseResponse.from(stock.getWarehouse()) : null,
            stock.getMaterial() != null ? MaterialResponse.from(stock.getMaterial()) : null,
            stock.getQuantity(),
            stock.getReservedQuantity(),
            stock.getLastUpdated()
        );
    }
}
