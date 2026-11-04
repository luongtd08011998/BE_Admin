package vn.hoidanit.springrestwithai.gv.recurring;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.gv.auditlog.GvAuditAction;
import vn.hoidanit.springrestwithai.gv.recurring.dto.CreateGvRecurringRequest;
import vn.hoidanit.springrestwithai.gv.recurring.dto.GvRecurringResponse;
import vn.hoidanit.springrestwithai.gv.recurring.dto.UpdateGvRecurringRequest;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;

@RestController
@RequestMapping("/api/v1/gv/recurring-templates")
public class GvRecurringTemplateController {

    private final GvRecurringTemplateService recurringTemplateService;

    public GvRecurringTemplateController(GvRecurringTemplateService recurringTemplateService) {
        this.recurringTemplateService = recurringTemplateService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<GvRecurringResponse>>> getActiveTemplates() {
        return ResponseEntity.ok(ApiResponse.success(recurringTemplateService.getAllActive()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GvRecurringResponse>>> getAllTemplates() {
        return ResponseEntity.ok(ApiResponse.success(recurringTemplateService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GvRecurringResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(recurringTemplateService.getById(id)));
    }

    @PostMapping
    @GvAuditAction(action = "CREATE_RECURRING_TEMPLATE", entity = "GvRecurringTemplate", description = "Tạo khuôn mẫu việc định kỳ")
    public ResponseEntity<ApiResponse<GvRecurringResponse>> create(@Valid @RequestBody CreateGvRecurringRequest request) {
        Long creatorId = GvSecurityUtil.getRequiredCurrentUserId();
        GvRecurringResponse response = recurringTemplateService.create(creatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @GvAuditAction(action = "UPDATE_RECURRING_TEMPLATE", entity = "GvRecurringTemplate", description = "Cập nhật khuôn mẫu việc định kỳ")
    public ResponseEntity<ApiResponse<GvRecurringResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGvRecurringRequest request) {
        return ResponseEntity.ok(ApiResponse.success(recurringTemplateService.update(id, request)));
    }

    @PatchMapping("/{id}/toggle-active")
    @GvAuditAction(action = "TOGGLE_RECURRING_TEMPLATE", entity = "GvRecurringTemplate", description = "Bật/tắt việc định kỳ")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long id) {
        recurringTemplateService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @GvAuditAction(action = "DELETE_RECURRING_TEMPLATE", entity = "GvRecurringTemplate", description = "Xóa việc định kỳ")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        recurringTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
