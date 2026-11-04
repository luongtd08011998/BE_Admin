package vn.hoidanit.springrestwithai.gv.report;

import vn.hoidanit.springrestwithai.gv.report.dto.GvPerformanceReportResponse;

public interface GvReportService {
    GvPerformanceReportResponse getEmployeePerformanceReport(Long departmentId, Long userId, Integer month, Integer year);
}
