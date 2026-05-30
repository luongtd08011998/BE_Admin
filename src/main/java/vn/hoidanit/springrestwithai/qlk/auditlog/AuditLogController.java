package vn.hoidanit.springrestwithai.qlk.auditlog;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;

/**
 * REST API để xem Audit Logs của module QLK.
 * <p>
 * Endpoints:
 * <ul>
 *   <li>GET /api/v1/qlk/audit-logs                         - Lấy tất cả với filter tùy chọn</li>
 *   <li>GET /api/v1/qlk/audit-logs/users/{userId}          - Lấy log theo user</li>
 *   <li>GET /api/v1/qlk/audit-logs/entities/{name}         - Lấy log theo entity type</li>
 *   <li>GET /api/v1/qlk/audit-logs/entities/{name}/{id}    - Lấy log theo entity instance cụ thể</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/qlk/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Lấy danh sách audit log với filter tổng hợp.
     *
     * @param entityName filter theo tên entity (tùy chọn)
     * @param action     filter theo action (tùy chọn)
     * @param userId     filter theo userId (tùy chọn)
     * @param pageable   thông tin phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @ParameterObject Pageable pageable) {

        Page<AuditLog> page = auditLogService.findWithFilters(entityName, action, userId, pageable);
        Page<AuditLogResponse> mapped = page.map(AuditLogResponse::from);
        return ResponseEntity.ok(ApiResponse.success(ResultPaginationDTO.fromPage(mapped)));
    }

    /**
     * Lấy audit log theo user cụ thể.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getByUser(
            @PathVariable Long userId,
            @ParameterObject Pageable pageable) {

        Page<AuditLog> page = auditLogService.findByUser(userId, pageable);
        Page<AuditLogResponse> mapped = page.map(AuditLogResponse::from);
        return ResponseEntity.ok(ApiResponse.success(ResultPaginationDTO.fromPage(mapped)));
    }

    /**
     * Lấy audit log theo loại entity (ví dụ: "StockVoucher", "Warehouse").
     */
    @GetMapping("/entities/{entityName}")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getByEntityType(
            @PathVariable String entityName,
            @ParameterObject Pageable pageable) {

        Page<AuditLog> page = auditLogService.findByEntity(entityName, null, pageable);
        Page<AuditLogResponse> mapped = page.map(AuditLogResponse::from);
        return ResponseEntity.ok(ApiResponse.success(ResultPaginationDTO.fromPage(mapped)));
    }

    /**
     * Lấy audit log theo entity instance cụ thể (ví dụ: StockVoucher id=42).
     */
    @GetMapping("/entities/{entityName}/{entityId}")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getByEntityInstance(
            @PathVariable String entityName,
            @PathVariable Long entityId,
            @ParameterObject Pageable pageable) {

        Page<AuditLog> page = auditLogService.findByEntity(entityName, entityId, pageable);
        Page<AuditLogResponse> mapped = page.map(AuditLogResponse::from);
        return ResponseEntity.ok(ApiResponse.success(ResultPaginationDTO.fromPage(mapped)));
    }
}
