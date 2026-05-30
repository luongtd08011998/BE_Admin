package vn.hoidanit.springrestwithai.qlk.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.qlk.dashboard.dto.QlkDashboardResponse;

@RestController
@RequestMapping("/api/v1/qlk/dashboard")
public class QlkDashboardController {

    private final QlkDashboardService qlkDashboardService;

    public QlkDashboardController(QlkDashboardService qlkDashboardService) {
        this.qlkDashboardService = qlkDashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<QlkDashboardResponse>> getDashboard() {
        QlkDashboardResponse response = qlkDashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin dashboard quản lý kho thành công", response));
    }
}
