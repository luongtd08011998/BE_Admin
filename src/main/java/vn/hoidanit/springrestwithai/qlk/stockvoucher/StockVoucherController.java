package vn.hoidanit.springrestwithai.qlk.stockvoucher;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlk.auditlog.AuditAction;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.CreateStockVoucherRequest;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.StockVoucherResponse;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.UpdateStockVoucherRequest;

@RestController
@RequestMapping("/api/v1/qlk/warehouses/{warehouseId}/vouchers")
public class StockVoucherController {

    private final StockVoucherService stockVoucherService;

    public StockVoucherController(StockVoucherService stockVoucherService) {
        this.stockVoucherService = stockVoucherService;
    }

    @PostMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('CREATE_STOCK_VOUCHER')")
    @AuditAction(action = "CREATE_STOCK_VOUCHER", entity = "StockVoucher", description = "Tạo phiếu kho mới")
    public ResponseEntity<ApiResponse<StockVoucherResponse>> create(
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateStockVoucherRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        StockVoucherResponse result = stockVoucherService.create(request, userId, warehouseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('UPDATE_STOCK_VOUCHER')")
    @AuditAction(action = "UPDATE_STOCK_VOUCHER", entity = "StockVoucher", description = "Cập nhật phiếu kho")
    public ResponseEntity<ApiResponse<StockVoucherResponse>> update(
            @PathVariable Long warehouseId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockVoucherRequest request) {
        StockVoucherResponse result = stockVoucherService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('DELETE_STOCK_VOUCHER')")
    @AuditAction(action = "DELETE_STOCK_VOUCHER", entity = "StockVoucher", description = "Xóa phiếu kho")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long warehouseId,
            @PathVariable Long id) {
        stockVoucherService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_STOCK_VOUCHER')")
    public ResponseEntity<ApiResponse<StockVoucherResponse>> getById(
            @PathVariable Long warehouseId,
            @PathVariable Long id) {
        StockVoucherResponse result = stockVoucherService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_STOCK_VOUCHERS')")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(
            @PathVariable Long warehouseId,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        if (warehouseId == null) {
            return ResponseEntity.ok(ApiResponse.success(ResultPaginationDTO.fromPage(Page.empty())));
        }
        ResultPaginationDTO result = stockVoucherService.getAll(pageable, warehouseId, type, status);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('SUBMIT_STOCK_VOUCHER')")
    @AuditAction(action = "SUBMIT_STOCK_VOUCHER", entity = "StockVoucher", description = "Gửi phiếu kho chờ duyệt")
    public ResponseEntity<ApiResponse<StockVoucherResponse>> submit(
            @PathVariable Long warehouseId,
            @PathVariable Long id) {
        StockVoucherResponse result = stockVoucherService.submit(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('APPROVE_STOCK_VOUCHER')")
    @AuditAction(action = "APPROVE_STOCK_VOUCHER", entity = "StockVoucher", description = "Phê duyệt phiếu kho")
    public ResponseEntity<ApiResponse<StockVoucherResponse>> approve(
            @PathVariable Long warehouseId,
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long approverId = jwt.getClaim("userId");
        StockVoucherResponse result = stockVoucherService.approve(id, approverId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('REJECT_STOCK_VOUCHER')")
    @AuditAction(action = "REJECT_STOCK_VOUCHER", entity = "StockVoucher", description = "Từ chối phiếu kho")
    public ResponseEntity<ApiResponse<StockVoucherResponse>> reject(
            @PathVariable Long warehouseId,
            @PathVariable Long id,
            @RequestParam String reason) {
        StockVoucherResponse result = stockVoucherService.reject(id, reason);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
