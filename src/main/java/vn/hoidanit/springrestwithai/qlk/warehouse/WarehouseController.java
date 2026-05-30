package vn.hoidanit.springrestwithai.qlk.warehouse;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;
import vn.hoidanit.springrestwithai.qlk.auditlog.AuditAction;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.AssignWarehouseUsersRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.CreateWarehouseRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.UpdateWarehouseRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.WarehouseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/qlk/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    /**
     * PUBLIC endpoint for login dropdown
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllActiveWarehouses() {
        List<WarehouseResponse> result = warehouseService.getAllActiveWarehouses();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('CREATE_WAREHOUSE')")
    @AuditAction(action = "CREATE_WAREHOUSE", entity = "Warehouse", description = "Tạo kho hàng mới")
    public ResponseEntity<ApiResponse<WarehouseResponse>> create(@Valid @RequestBody CreateWarehouseRequest request) {
        WarehouseResponse result = warehouseService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('UPDATE_WAREHOUSE')")
    @AuditAction(action = "UPDATE_WAREHOUSE", entity = "Warehouse", description = "Cập nhật thông tin kho")
    public ResponseEntity<ApiResponse<WarehouseResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateWarehouseRequest request) {
        WarehouseResponse result = warehouseService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}/alert-threshold")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('UPDATE_WAREHOUSE') or hasRole('GIAM_DOC') or hasRole('SUPER_ADMIN')")
    @AuditAction(action = "UPDATE_WAREHOUSE_THRESHOLD", entity = "Warehouse", description = "Cập nhật mức cảnh báo tồn kho")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateAlertThreshold(
            @PathVariable Long id, 
            @RequestParam(name = "value", defaultValue = "10") Integer value) {
        WarehouseResponse result = warehouseService.updateAlertThreshold(id, value);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('DELETE_WAREHOUSE')")
    @AuditAction(action = "DELETE_WAREHOUSE", entity = "Warehouse", description = "Xóa kho hàng")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_WAREHOUSE')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getById(@PathVariable Long id) {
        WarehouseResponse result = warehouseService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/page")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_WAREHOUSE')")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getPage(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = warehouseService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/users")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('ASSIGN_WAREHOUSE_USERS')")
    @AuditAction(action = "ASSIGN_WAREHOUSE_USERS", entity = "Warehouse", description = "Phân công user vào kho")
    public ResponseEntity<ApiResponse<Void>> assignUsers(@PathVariable Long id, @Valid @RequestBody AssignWarehouseUsersRequest request) {
        warehouseService.assignUsers(id, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_WAREHOUSE_USERS')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAssignedUsers(@PathVariable Long id) {
        List<UserResponse> result = warehouseService.getAssignedUsers(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
