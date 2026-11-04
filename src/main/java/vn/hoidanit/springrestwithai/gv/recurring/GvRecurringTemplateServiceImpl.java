package vn.hoidanit.springrestwithai.gv.recurring;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.gv.department.GvDepartment;
import vn.hoidanit.springrestwithai.gv.department.GvDepartmentRepository;
import vn.hoidanit.springrestwithai.gv.recurring.dto.CreateGvRecurringRequest;
import vn.hoidanit.springrestwithai.gv.recurring.dto.GvRecurringResponse;
import vn.hoidanit.springrestwithai.gv.recurring.dto.UpdateGvRecurringRequest;

@Service
@Transactional
public class GvRecurringTemplateServiceImpl implements GvRecurringTemplateService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final GvRecurringTemplateRepository templateRepository;
    private final GvDepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public GvRecurringTemplateServiceImpl(
            GvRecurringTemplateRepository templateRepository,
            GvDepartmentRepository departmentRepository,
            UserRepository userRepository) {
        this.templateRepository = templateRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvRecurringResponse> getAllActive() {
        return templateRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvRecurringResponse> getAll() {
        return templateRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GvRecurringResponse getById(Long id) {
        GvRecurringTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuôn mẫu việc định kỳ", "id", id));
        return toResponse(template);
    }

    @Override
    public GvRecurringResponse create(Long creatorId, CreateGvRecurringRequest request) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", creatorId));

        User defaultAssignee = userRepository.findById(request.defaultAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Người nhận việc mặc định", "id", request.defaultAssigneeId()));

        GvDepartment department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", request.departmentId()));
        }

        Instant nextRun = computeNextRun(request.frequency(), request.dayOfWeek(), request.dayOfMonth(), request.timeOfDay());

        GvRecurringTemplate template = GvRecurringTemplate.builder()
                .title(request.title().trim())
                .description(request.description())
                .department(department)
                .creator(creator)
                .defaultAssignee(defaultAssignee)
                .frequency(request.frequency())
                .cronExpression(request.cronExpression())
                .dayOfWeek(request.dayOfWeek())
                .dayOfMonth(request.dayOfMonth())
                .timeOfDay(request.timeOfDay() != null ? request.timeOfDay() : "08:00")
                .deadlineDaysOffset(request.deadlineDaysOffset() != null ? request.deadlineDaysOffset() : 1)
                .overdueAction(request.overdueAction() != null ? request.overdueAction() : GvOverdueAction.ALERT_AND_CREATE)
                .isActive(true)
                .nextRunAt(nextRun)
                .build();

        return toResponse(templateRepository.save(template));
    }

    @Override
    public GvRecurringResponse update(Long id, UpdateGvRecurringRequest request) {
        GvRecurringTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuôn mẫu việc định kỳ", "id", id));

        User defaultAssignee = userRepository.findById(request.defaultAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Người nhận việc mặc định", "id", request.defaultAssigneeId()));

        if (request.departmentId() != null) {
            GvDepartment department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", "id", request.departmentId()));
            template.setDepartment(department);
        } else {
            template.setDepartment(null);
        }

        template.setTitle(request.title().trim());
        template.setDescription(request.description());
        template.setDefaultAssignee(defaultAssignee);
        template.setFrequency(request.frequency());
        template.setCronExpression(request.cronExpression());
        template.setDayOfWeek(request.dayOfWeek());
        template.setDayOfMonth(request.dayOfMonth());
        template.setTimeOfDay(request.timeOfDay() != null ? request.timeOfDay() : "08:00");
        template.setDeadlineDaysOffset(request.deadlineDaysOffset() != null ? request.deadlineDaysOffset() : 1);
        template.setOverdueAction(request.overdueAction());
        if (request.isActive() != null) {
            template.setIsActive(request.isActive());
        }

        Instant nextRun = computeNextRun(request.frequency(), request.dayOfWeek(), request.dayOfMonth(), template.getTimeOfDay());
        template.setNextRunAt(nextRun);

        return toResponse(templateRepository.save(template));
    }

    @Override
    public void toggleActive(Long id) {
        GvRecurringTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuôn mẫu việc định kỳ", "id", id));
        template.setIsActive(!Boolean.TRUE.equals(template.getIsActive()));
        templateRepository.save(template);
    }

    @Override
    public void delete(Long id) {
        GvRecurringTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khuôn mẫu việc định kỳ", "id", id));
        template.setIsActive(false);
        templateRepository.save(template);
    }

    private Instant computeNextRun(GvFrequencyType frequency, Integer dayOfWeek, Integer dayOfMonth, String timeOfDay) {
        LocalTime time = LocalTime.of(8, 0);
        if (timeOfDay != null && timeOfDay.contains(":")) {
            String[] parts = timeOfDay.split(":");
            try {
                time = LocalTime.of(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
            } catch (Exception ignored) {}
        }

        ZonedDateTime now = ZonedDateTime.now(DEFAULT_ZONE);
        ZonedDateTime candidate = now.with(time);

        switch (frequency) {
            case DAILY -> {
                if (candidate.isBefore(now)) {
                    candidate = candidate.plusDays(1);
                }
            }
            case WEEKLY -> {
                int targetDay = (dayOfWeek != null && dayOfWeek >= 1 && dayOfWeek <= 7) ? dayOfWeek : 1;
                DayOfWeek dow = DayOfWeek.of(targetDay);
                candidate = now.with(dow).with(time);
                if (candidate.isBefore(now)) {
                    candidate = candidate.plusWeeks(1);
                }
            }
            case MONTHLY -> {
                int targetDom = (dayOfMonth != null && dayOfMonth >= 1 && dayOfMonth <= 31) ? dayOfMonth : 1;
                LocalDate targetDate = now.toLocalDate().withDayOfMonth(Math.min(targetDom, now.toLocalDate().lengthOfMonth()));
                candidate = targetDate.atTime(time).atZone(DEFAULT_ZONE);
                if (candidate.isBefore(now)) {
                    candidate = candidate.plusMonths(1);
                }
            }
            case QUARTERLY -> {
                int targetDom = (dayOfMonth != null && dayOfMonth >= 1 && dayOfMonth <= 31) ? dayOfMonth : 1;
                LocalDate targetDate = now.toLocalDate().withDayOfMonth(Math.min(targetDom, now.toLocalDate().lengthOfMonth()));
                candidate = targetDate.atTime(time).atZone(DEFAULT_ZONE);
                if (candidate.isBefore(now)) {
                    candidate = candidate.plusMonths(3);
                }
            }
        }

        return candidate.toInstant();
    }

    private GvRecurringResponse toResponse(GvRecurringTemplate t) {
        return GvRecurringResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .departmentId(t.getDepartment() != null ? t.getDepartment().getId() : null)
                .departmentName(t.getDepartment() != null ? t.getDepartment().getName() : null)
                .creatorId(t.getCreator().getId())
                .creatorName(t.getCreator().getName())
                .defaultAssigneeId(t.getDefaultAssignee().getId())
                .defaultAssigneeName(t.getDefaultAssignee().getName())
                .frequency(t.getFrequency())
                .cronExpression(t.getCronExpression())
                .dayOfWeek(t.getDayOfWeek())
                .dayOfMonth(t.getDayOfMonth())
                .timeOfDay(t.getTimeOfDay())
                .deadlineDaysOffset(t.getDeadlineDaysOffset())
                .overdueAction(t.getOverdueAction())
                .isActive(t.getIsActive())
                .lastGeneratedAt(t.getLastGeneratedAt())
                .nextRunAt(t.getNextRunAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
