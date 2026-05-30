package vn.hoidanit.springrestwithai.qlk.category;

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
import vn.hoidanit.springrestwithai.qlk.category.dto.CreateQlkCategoryRequest;
import vn.hoidanit.springrestwithai.qlk.category.dto.QlkCategoryResponse;
import vn.hoidanit.springrestwithai.qlk.category.dto.UpdateQlkCategoryRequest;

@RestController
@RequestMapping("/api/v1/qlk/categories")
public class QlkCategoryController {

    private final QlkCategoryService qlkCategoryService;

    public QlkCategoryController(QlkCategoryService qlkCategoryService) {
        this.qlkCategoryService = qlkCategoryService;
    }

    @PostMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('CREATE_QLK_CATEGORY')")
    @AuditAction(action = "CREATE_QLK_CATEGORY", entity = "QlkCategory", description = "Tạo danh mục vật tư mới")
    public ResponseEntity<ApiResponse<QlkCategoryResponse>> create(@Valid @RequestBody CreateQlkCategoryRequest request) {
        QlkCategoryResponse result = qlkCategoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('UPDATE_QLK_CATEGORY')")
    @AuditAction(action = "UPDATE_QLK_CATEGORY", entity = "QlkCategory", description = "Cập nhật danh mục vật tư")
    public ResponseEntity<ApiResponse<QlkCategoryResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateQlkCategoryRequest request) {
        QlkCategoryResponse result = qlkCategoryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('DELETE_QLK_CATEGORY')")
    @AuditAction(action = "DELETE_QLK_CATEGORY", entity = "QlkCategory", description = "Xóa danh mục vật tư")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        qlkCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_QLK_CATEGORY')")
    public ResponseEntity<ApiResponse<QlkCategoryResponse>> getById(@PathVariable Long id) {
        QlkCategoryResponse result = qlkCategoryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_QLK_CATEGORIES')")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = qlkCategoryService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
