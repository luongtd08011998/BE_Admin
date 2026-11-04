package vn.hoidanit.springrestwithai.gv.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.gv.task.GvTaskService;
import vn.hoidanit.springrestwithai.gv.task.dto.GvDashboardSummaryResponse;

/**
 * Controller cho Dashboard tổng quan toàn công ty.
 * Endpoint: GET /api/v1/gv/dashboard/summary
 * Được App gọi khi người dùng là SuperAdmin.
 */
@RestController
@RequestMapping("/api/v1/gv/dashboard")
public class GvDashboardController {

    private final GvTaskService taskService;

    public GvDashboardController(GvTaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<GvDashboardSummaryResponse>> getDashboardSummary() {
        GvDashboardSummaryResponse summary = taskService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê tổng quan toàn công ty thành công", summary));
    }
}
