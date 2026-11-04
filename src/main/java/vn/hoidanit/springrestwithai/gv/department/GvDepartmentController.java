package vn.hoidanit.springrestwithai.gv.department;

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
import vn.hoidanit.springrestwithai.gv.department.dto.CreateGvDepartmentRequest;
import vn.hoidanit.springrestwithai.gv.department.dto.GvDepartmentResponse;
import vn.hoidanit.springrestwithai.gv.department.dto.UpdateGvDepartmentRequest;

@RestController
@RequestMapping("/api/v1/gv/departments")
public class GvDepartmentController {

    private final GvDepartmentService departmentService;

    public GvDepartmentController(GvDepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<GvDepartmentResponse>>> getActiveDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllActive()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GvDepartmentResponse>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GvDepartmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getById(id)));
    }

    @PostMapping
    @GvAuditAction(action = "CREATE_DEPARTMENT", entity = "GvDepartment", description = "Tạo phòng ban giao việc mới")
    public ResponseEntity<ApiResponse<GvDepartmentResponse>> create(@Valid @RequestBody CreateGvDepartmentRequest request) {
        GvDepartmentResponse response = departmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @GvAuditAction(action = "UPDATE_DEPARTMENT", entity = "GvDepartment", description = "Cập nhật phòng ban giao việc")
    public ResponseEntity<ApiResponse<GvDepartmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGvDepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @GvAuditAction(action = "DELETE_DEPARTMENT", entity = "GvDepartment", description = "Ngừng kích hoạt phòng ban")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
