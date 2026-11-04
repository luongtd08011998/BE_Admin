package vn.hoidanit.springrestwithai.gv.checklist;

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
import vn.hoidanit.springrestwithai.gv.checklist.dto.CreateGvChecklistRequest;
import vn.hoidanit.springrestwithai.gv.checklist.dto.GvChecklistResponse;
import vn.hoidanit.springrestwithai.gv.checklist.dto.UpdateGvChecklistRequest;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;

@RestController
@RequestMapping("/api/v1/gv/tasks/{taskId}/checklists")
public class GvChecklistController {

    private final GvChecklistService checklistService;

    public GvChecklistController(GvChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GvChecklistResponse>>> getByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(checklistService.getByTaskId(taskId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GvChecklistResponse>> create(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateGvChecklistRequest request) {
        GvChecklistResponse response = checklistService.create(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{checklistId}")
    public ResponseEntity<ApiResponse<GvChecklistResponse>> update(
            @PathVariable Long taskId,
            @PathVariable Long checklistId,
            @Valid @RequestBody UpdateGvChecklistRequest request) {
        return ResponseEntity.ok(ApiResponse.success(checklistService.update(checklistId, request)));
    }

    @PatchMapping("/{checklistId}/toggle")
    public ResponseEntity<ApiResponse<GvChecklistResponse>> toggle(
            @PathVariable Long taskId,
            @PathVariable Long checklistId) {
        Long userId = GvSecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(checklistService.toggle(checklistId, userId)));
    }

    @DeleteMapping("/{checklistId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long taskId,
            @PathVariable Long checklistId) {
        checklistService.delete(checklistId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
