package vn.hoidanit.springrestwithai.gv.label;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.gv.auditlog.GvAuditAction;
import vn.hoidanit.springrestwithai.gv.label.dto.CreateGvLabelRequest;
import vn.hoidanit.springrestwithai.gv.label.dto.GvLabelResponse;
import vn.hoidanit.springrestwithai.gv.label.dto.UpdateGvLabelRequest;

@RestController
@RequestMapping("/api/v1/gv/labels")
public class GvLabelController {

    private final GvLabelService labelService;

    public GvLabelController(GvLabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<GvLabelResponse>>> getActiveLabels() {
        return ResponseEntity.ok(ApiResponse.success(labelService.getAllActive()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GvLabelResponse>>> getAllLabels() {
        return ResponseEntity.ok(ApiResponse.success(labelService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GvLabelResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(labelService.getById(id)));
    }

    @PostMapping
    @GvAuditAction(action = "CREATE_LABEL", entity = "GvLabel", description = "Tạo nhãn công việc mới")
    public ResponseEntity<ApiResponse<GvLabelResponse>> create(@Valid @RequestBody CreateGvLabelRequest request) {
        GvLabelResponse response = labelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @GvAuditAction(action = "UPDATE_LABEL", entity = "GvLabel", description = "Cập nhật nhãn công việc")
    public ResponseEntity<ApiResponse<GvLabelResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGvLabelRequest request) {
        return ResponseEntity.ok(ApiResponse.success(labelService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @GvAuditAction(action = "DELETE_LABEL", entity = "GvLabel", description = "Ngừng kích hoạt nhãn công việc")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        labelService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
