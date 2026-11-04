package vn.hoidanit.springrestwithai.gv.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;

@RestController
@RequestMapping("/api/v1/gv/audit-logs")
public class GvAuditLogController {

    private final GvAuditLogService auditLogService;

    public GvAuditLogController(GvAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GvAuditLog>>> getEntityLogs(
            @RequestParam(defaultValue = "GvTask") String entityName,
            @RequestParam(required = false) Long entityId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(auditLogService.findByEntity(entityName, entityId, pageable)));
    }

    @GetMapping("/my-logs")
    public ResponseEntity<ApiResponse<Page<GvAuditLog>>> getMyLogs(Pageable pageable) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(auditLogService.findByUser(userId, pageable)));
    }
}
