package vn.hoidanit.springrestwithai.gv.report.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record GvPerformanceReportResponse(
    Long departmentId,
    String departmentName,
    Integer month,
    Integer year,
    String periodTitle,
    int totalEmployees,
    int totalTasks,
    int totalCompletedTasks,
    double departmentAverageKpi,
    double departmentAverageRating,
    double departmentCompletionRate,
    List<GvEmployeePerformanceDto> employees
) {}
