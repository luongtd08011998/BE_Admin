package vn.hoidanit.springrestwithai.gv.report;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.gv.report.dto.GvPerformanceReportResponse;

@RestController
@RequestMapping("/api/v1/gv/reports")
public class GvReportController {

    private final GvReportService reportService;

    public GvReportController(GvReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/employee-performance")
    public ResponseEntity<ApiResponse<GvPerformanceReportResponse>> getEmployeePerformanceReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        GvPerformanceReportResponse report = reportService.getEmployeePerformanceReport(departmentId, userId, month, year);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo hiệu quả làm việc nhân sự thành công", report));
    }
}
