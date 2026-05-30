package vn.hoidanit.springrestwithai.qlk.inventory;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;

@RestController
@RequestMapping("/api/v1/qlk/warehouses/{warehouseId}/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/stocks")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_INVENTORY')")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getStocks(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = inventoryService.getStocksByWarehouse(warehouseId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/transactions")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_INVENTORY_TX')")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getTransactions(
            @PathVariable Long warehouseId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String date,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = inventoryService.getTransactionsByWarehouse(warehouseId, search, type, date, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/snapshots")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_INVENTORY_SNAPSHOT')")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getSnapshots(
            @PathVariable Long warehouseId,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = inventoryService.getSnapshotsByWarehouse(warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
