package vn.hoidanit.springrestwithai.qlk.inventory.dto;

import java.time.Instant;
import vn.hoidanit.springrestwithai.qlk.inventory.InventoryTransaction;
import vn.hoidanit.springrestwithai.qlk.inventory.TransactionType;
import vn.hoidanit.springrestwithai.qlk.material.dto.MaterialResponse;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.WarehouseResponse;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;

public record InventoryTransactionResponse(
    Long id,
    WarehouseResponse warehouse,
    MaterialResponse material,
    TransactionType transactionType,
    Integer quantityChange,
    Integer quantityBefore,
    Integer quantityAfter,
    Long voucherId,
    String voucherCode,
    String note,
    UserResponse createdBy,
    Instant createdAt
) {
    public static InventoryTransactionResponse from(InventoryTransaction transaction) {
        if (transaction == null) return null;
        return new InventoryTransactionResponse(
            transaction.getId(),
            transaction.getWarehouse() != null ? WarehouseResponse.from(transaction.getWarehouse()) : null,
            transaction.getMaterial() != null ? MaterialResponse.from(transaction.getMaterial()) : null,
            transaction.getTransactionType(),
            transaction.getQuantityChange(),
            transaction.getQuantityBefore(),
            transaction.getQuantityAfter(),
            transaction.getStockVoucher() != null ? transaction.getStockVoucher().getId() : null,
            transaction.getStockVoucher() != null ? transaction.getStockVoucher().getVoucherCode() : null,
            transaction.getNote(),
            transaction.getCreatedBy() != null ? UserResponse.fromEntity(transaction.getCreatedBy()) : null,
            transaction.getCreatedAt()
        );
    }
}
