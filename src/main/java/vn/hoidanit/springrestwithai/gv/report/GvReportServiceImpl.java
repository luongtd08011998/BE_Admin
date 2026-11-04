package vn.hoidanit.springrestwithai.gv.report;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.gv.department.GvDepartment;
import vn.hoidanit.springrestwithai.gv.department.GvDepartmentRepository;
import vn.hoidanit.springrestwithai.gv.report.dto.GvEmployeePerformanceDto;
import vn.hoidanit.springrestwithai.gv.report.dto.GvPerformanceReportResponse;
import vn.hoidanit.springrestwithai.gv.task.GvTask;
import vn.hoidanit.springrestwithai.gv.task.GvTaskRepository;
import vn.hoidanit.springrestwithai.gv.task.GvTaskPriority;
import vn.hoidanit.springrestwithai.gv.task.GvTaskStatus;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;

@Service
@Transactional(readOnly = true)
public class GvReportServiceImpl implements GvReportService {

    private final GvTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GvDepartmentRepository departmentRepository;

    public GvReportServiceImpl(
            GvTaskRepository taskRepository,
            UserRepository userRepository,
            GvDepartmentRepository departmentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public GvPerformanceReportResponse getEmployeePerformanceReport(
            Long departmentId, Long userId, Integer month, Integer year) {
        Long currentUserId = GvSecurityUtil.getCurrentUserId();
        boolean isSuperAdmin = GvSecurityUtil.isSuperAdmin();

        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        boolean isManager = currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(r -> {
                    String name = r.getName().toUpperCase();
                    return name.contains("MANAGER") || name.contains("TRUONG_PHONG") || name.contains("GIAM_DOC");
                });
        boolean isAdmin = currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ADMIN") || r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        Long targetDeptId = departmentId;
        Long targetUserId = userId;

        if (!isSuperAdmin && !isAdmin) {
            if (isManager && currentUser != null && currentUser.getDepartment() != null) {
                // Manager: giới hạn trong phòng ban của mình
                targetDeptId = currentUser.getDepartment().getId();
            } else if (currentUserId != null) {
                // User thường: chỉ xem của chính mình
                targetUserId = currentUserId;
                targetDeptId = currentUser != null && currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
            }
        }

        List<User> targetUsers;
        if (targetUserId != null) {
            targetUsers = userRepository.findById(targetUserId).map(List::of).orElse(List.of());
        } else if (targetDeptId != null) {
            targetUsers = userRepository.findByDepartmentId(targetDeptId);
        } else {
            targetUsers = userRepository.findAll();
        }

        Instant now = Instant.now();
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");

        String periodTitle;
        if (month != null && year != null) {
            periodTitle = "Tháng " + month + "/" + year;
        } else if (year != null) {
            periodTitle = "Năm " + year;
        } else if (month != null) {
            periodTitle = "Tháng " + month;
        } else {
            periodTitle = "Toàn thời gian";
        }

        List<GvEmployeePerformanceDto> employeeList = new ArrayList<>();

        int totalCompanyTasks = 0;
        int totalCompanyCompleted = 0;
        double sumKpi = 0.0;
        int countKpi = 0;
        double sumRating = 0.0;
        int countRating = 0;

        final Long filterDeptId = targetDeptId;
        for (User u : targetUsers) {
            List<GvTask> userTasks = taskRepository.findByAssigneeId(u.getId());
            if (filterDeptId != null) {
                userTasks = userTasks.stream()
                        .filter(t -> t.getDepartment() != null && t.getDepartment().getId().equals(filterDeptId))
                        .toList();
            }

            // Lọc theo Tháng và Năm (dựa trên mốc thời gian hoàn thành hoặc tạo task)
            if (year != null || month != null) {
                userTasks = userTasks.stream()
                        .filter(t -> {
                            Instant dateToCompare;
                            if (t.getStatus() == GvTaskStatus.DONE) {
                                dateToCompare = t.getCompletedAt() != null ? t.getCompletedAt() :
                                        (t.getReviewedAt() != null ? t.getReviewedAt() : t.getCreatedAt());
                            } else {
                                dateToCompare = t.getCreatedAt() != null ? t.getCreatedAt() : t.getDueDate();
                            }
                            if (dateToCompare == null) return false;
                            ZonedDateTime zdt = dateToCompare.atZone(vnZone);
                            if (year != null && zdt.getYear() != year) return false;
                            if (month != null && zdt.getMonthValue() != month) return false;
                            return true;
                        })
                        .toList();
            }

            int totalTasks = userTasks.size();
            List<GvTask> completedTasks = userTasks.stream()
                    .filter(t -> t.getStatus() == GvTaskStatus.DONE)
                    .toList();
            int completedCount = completedTasks.size();

            int inProgressCount = (int) userTasks.stream()
                    .filter(t -> t.getStatus() == GvTaskStatus.IN_PROGRESS)
                    .count();

            int pendingReviewCount = (int) userTasks.stream()
                    .filter(t -> t.getStatus() == GvTaskStatus.PENDING_REVIEW)
                    .count();

            int overdueCount = (int) userTasks.stream()
                    .filter(t -> t.getStatus() != GvTaskStatus.DONE && t.getStatus() != GvTaskStatus.CANCELLED
                            && t.getDueDate() != null && t.getDueDate().isBefore(now))
                    .count();

            List<GvTask> ratedTasks = completedTasks.stream()
                    .filter(t -> t.getOverallScore() != null || t.getRating() != null)
                    .toList();

            double avgRating = 0.0;
            double avgKpi = 0.0;
            double avgQuality = 0.0;
            double avgTimeliness = 0.0;

            if (!ratedTasks.isEmpty()) {
                avgRating = Math.round(ratedTasks.stream().mapToDouble(t -> t.getRating() != null ? t.getRating() : 0.0).average().orElse(0.0) * 10.0) / 10.0;
                avgKpi = Math.round(ratedTasks.stream().mapToDouble(t -> t.getOverallScore() != null ? t.getOverallScore() : 0.0).average().orElse(0.0) * 10.0) / 10.0;
                avgQuality = Math.round(ratedTasks.stream().mapToDouble(t -> t.getQualityScore() != null ? t.getQualityScore() : 0.0).average().orElse(0.0) * 10.0) / 10.0;
                avgTimeliness = Math.round(ratedTasks.stream().mapToDouble(t -> t.getTimelinessScore() != null ? t.getTimelinessScore() : 0.0).average().orElse(0.0) * 10.0) / 10.0;

                sumKpi += avgKpi;
                countKpi++;
                sumRating += avgRating;
                countRating++;
            }

            int excellentCount = (int) completedTasks.stream()
                    .filter(t -> "EXCELLENT".equalsIgnoreCase(t.getEvaluationGrade()))
                    .count();
            int goodCount = (int) completedTasks.stream()
                    .filter(t -> "GOOD".equalsIgnoreCase(t.getEvaluationGrade()))
                    .count();
            int fairCount = (int) completedTasks.stream()
                    .filter(t -> "FAIR".equalsIgnoreCase(t.getEvaluationGrade()))
                    .count();
            int poorCount = (int) completedTasks.stream()
                    .filter(t -> "POOR".equalsIgnoreCase(t.getEvaluationGrade()))
                    .count();

            double completionRate = totalTasks > 0
                    ? Math.round(((double) completedCount / totalTasks * 100.0) * 10.0) / 10.0
                    : 0.0;

            // Phân loại Bắt buộc (HIGH/URGENT) vs Không bắt buộc (NORMAL/LOW)
            List<GvTask> mandatoryTasks = userTasks.stream()
                    .filter(t -> t.getPriority() == GvTaskPriority.HIGH || t.getPriority() == GvTaskPriority.URGENT)
                    .toList();
            List<GvTask> optionalTasks = userTasks.stream()
                    .filter(t -> t.getPriority() == GvTaskPriority.NORMAL || t.getPriority() == GvTaskPriority.LOW)
                    .toList();

            int mandatoryTotalTasks = mandatoryTasks.size();
            int mandatoryCompletedTasks = (int) mandatoryTasks.stream()
                    .filter(t -> t.getStatus() == GvTaskStatus.DONE)
                    .count();
            int optionalTotalTasks = optionalTasks.size();
            int optionalCompletedTasks = (int) optionalTasks.stream()
                    .filter(t -> t.getStatus() == GvTaskStatus.DONE)
                    .count();

            totalCompanyTasks += totalTasks;
            totalCompanyCompleted += completedCount;

            String roleTitle = "Nhân viên";
            if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                String firstRole = u.getRoles().get(0).getName();
                if (firstRole.contains("MANAGER") || firstRole.contains("TRUONG_PHONG")) roleTitle = "Trưởng phòng";
                else if (firstRole.contains("SUPER_ADMIN")) roleTitle = "Super Admin";
                else if (firstRole.contains("ADMIN")) roleTitle = "Quản trị viên";
            }

            String deptName = u.getDepartment() != null ? u.getDepartment().getName() : "Chung";

            employeeList.add(GvEmployeePerformanceDto.builder()
                    .userId(u.getId())
                    .displayName(u.getName())
                    .email(u.getEmail())
                    .roleTitle(roleTitle)
                    .departmentId(u.getDepartment() != null ? u.getDepartment().getId() : null)
                    .departmentName(deptName)
                    .totalTasks(totalTasks)
                    .completedTasks(completedCount)
                    .completionRate(completionRate)
                    .averageRating(avgRating)
                    .averageKpiScore(avgKpi)
                    .averageQualityScore(avgQuality)
                    .averageTimelinessScore(avgTimeliness)
                    .excellentCount(excellentCount)
                    .goodCount(goodCount)
                    .fairCount(fairCount)
                    .poorCount(poorCount)
                    .inProgressCount(inProgressCount)
                    .pendingReviewCount(pendingReviewCount)
                    .overdueCount(overdueCount)
                    .rank(0)
                    .mandatoryTotalTasks(mandatoryTotalTasks)
                    .mandatoryCompletedTasks(mandatoryCompletedTasks)
                    .optionalTotalTasks(optionalTotalTasks)
                    .optionalCompletedTasks(optionalCompletedTasks)
                    .build());
        }

        // Sắp xếp theo Điểm KPI giảm dần, nếu bằng thì theo số việc hoàn thành giảm dần
        employeeList.sort(Comparator
                .comparing(GvEmployeePerformanceDto::averageKpiScore).reversed()
                .thenComparing(Comparator.comparing(GvEmployeePerformanceDto::completedTasks).reversed()));

        // Gán rank 1, 2, 3...
        List<GvEmployeePerformanceDto> rankedList = new ArrayList<>();
        for (int i = 0; i < employeeList.size(); i++) {
            GvEmployeePerformanceDto emp = employeeList.get(i);
            rankedList.add(new GvEmployeePerformanceDto(
                    emp.userId(),
                    emp.displayName(),
                    emp.email(),
                    emp.roleTitle(),
                    emp.departmentId(),
                    emp.departmentName(),
                    emp.totalTasks(),
                    emp.completedTasks(),
                    emp.completionRate(),
                    emp.averageRating(),
                    emp.averageKpiScore(),
                    emp.averageQualityScore(),
                    emp.averageTimelinessScore(),
                    emp.excellentCount(),
                    emp.goodCount(),
                    emp.fairCount(),
                    emp.poorCount(),
                    emp.inProgressCount(),
                    emp.pendingReviewCount(),
                    emp.overdueCount(),
                    i + 1,
                    emp.mandatoryTotalTasks(),
                    emp.mandatoryCompletedTasks(),
                    emp.optionalTotalTasks(),
                    emp.optionalCompletedTasks()
            ));
        }

        String deptTitle = "Toàn công ty";
        if (targetDeptId != null) {
            deptTitle = departmentRepository.findById(targetDeptId)
                    .map(GvDepartment::getName)
                    .orElse("Phòng ban #" + targetDeptId);
        }

        double deptAvgKpi = countKpi > 0 ? Math.round((sumKpi / countKpi) * 10.0) / 10.0 : 0.0;
        double deptAvgRating = countRating > 0 ? Math.round((sumRating / countRating) * 10.0) / 10.0 : 0.0;
        double deptCompletionRate = totalCompanyTasks > 0
                ? Math.round(((double) totalCompanyCompleted / totalCompanyTasks * 100.0) * 10.0) / 10.0
                : 0.0;

        return GvPerformanceReportResponse.builder()
                .departmentId(targetDeptId)
                .departmentName(deptTitle)
                .month(month)
                .year(year)
                .periodTitle(periodTitle)
                .totalEmployees(rankedList.size())
                .totalTasks(totalCompanyTasks)
                .totalCompletedTasks(totalCompanyCompleted)
                .departmentAverageKpi(deptAvgKpi)
                .departmentAverageRating(deptAvgRating)
                .departmentCompletionRate(deptCompletionRate)
                .employees(rankedList)
                .build();
    }
}
