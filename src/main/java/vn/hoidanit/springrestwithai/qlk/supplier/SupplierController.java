package vn.hoidanit.springrestwithai.qlk.supplier;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlk.auditlog.AuditAction;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.CreateSupplierRequest;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.SupplierResponse;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.UpdateSupplierRequest;

@RestController
@RequestMapping("/api/v1/qlk/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('CREATE_SUPPLIER')")
    @AuditAction(action = "CREATE_SUPPLIER", entity = "Supplier", description = "Tạo nhà cung cấp mới")
    public ResponseEntity<ApiResponse<SupplierResponse>> create(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse result = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('UPDATE_SUPPLIER')")
    @AuditAction(action = "UPDATE_SUPPLIER", entity = "Supplier", description = "Cập nhật nhà cung cấp")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse result = supplierService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('DELETE_SUPPLIER')")
    @AuditAction(action = "DELETE_SUPPLIER", entity = "Supplier", description = "Xóa nhà cung cấp")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_SUPPLIER')")
    public ResponseEntity<ApiResponse<SupplierResponse>> getById(@PathVariable Long id) {
        SupplierResponse result = supplierService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_SUPPLIERS')")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = supplierService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
