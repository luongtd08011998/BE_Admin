package vn.hoidanit.springrestwithai.qlk.material;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
import vn.hoidanit.springrestwithai.qlk.material.dto.CreateMaterialRequest;
import vn.hoidanit.springrestwithai.qlk.material.dto.MaterialResponse;
import vn.hoidanit.springrestwithai.qlk.material.dto.UpdateMaterialRequest;

@RestController
@RequestMapping("/api/v1/qlk/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('CREATE_MATERIAL')")
    @AuditAction(action = "CREATE_MATERIAL", entity = "Material", description = "Tạo vật tư mới")
    public ResponseEntity<ApiResponse<MaterialResponse>> create(
            @Valid @RequestBody CreateMaterialRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        MaterialResponse result = materialService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('UPDATE_MATERIAL')")
    @AuditAction(action = "UPDATE_MATERIAL", entity = "Material", description = "Cập nhật vật tư")
    public ResponseEntity<ApiResponse<MaterialResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateMaterialRequest request) {
        MaterialResponse result = materialService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('DELETE_MATERIAL')")
    @AuditAction(action = "DELETE_MATERIAL", entity = "Material", description = "Xóa vật tư")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_MATERIAL')")
    public ResponseEntity<ApiResponse<MaterialResponse>> getById(@PathVariable Long id) {
        MaterialResponse result = materialService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationManager.hasPermission('VIEW_MATERIALS')")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(@ParameterObject Pageable pageable) {
        ResultPaginationDTO result = materialService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
