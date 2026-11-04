package vn.hoidanit.springrestwithai.gv.recurring;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.gv.notification.GvNotificationService;
import vn.hoidanit.springrestwithai.gv.notification.GvNotificationType;
import vn.hoidanit.springrestwithai.gv.task.GvTask;
import vn.hoidanit.springrestwithai.gv.task.GvTaskPriority;
import vn.hoidanit.springrestwithai.gv.task.GvTaskRepository;
import vn.hoidanit.springrestwithai.gv.task.GvTaskStatus;

@Component
public class GvRecurringTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(GvRecurringTaskScheduler.class);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final GvRecurringTemplateRepository templateRepository;
    private final GvTaskRepository taskRepository;
    private final GvNotificationService notificationService;

    public GvRecurringTaskScheduler(
            GvRecurringTemplateRepository templateRepository,
            GvTaskRepository taskRepository,
            GvNotificationService notificationService) {
        this.templateRepository = templateRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    /**
     * Chạy định kỳ vào 06:00 mỗi sáng để quét và tự động sinh task định kỳ.
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void generateRecurringTasks() {
        Instant now = Instant.now();
        List<GvRecurringTemplate> templates = templateRepository.findByIsActiveTrueAndNextRunAtBefore(now);
        log.info("[GvRecurringScheduler] Tìm thấy {} khuôn mẫu định kỳ cần sinh task", templates.size());

        for (GvRecurringTemplate t : templates) {
            try {
                processTemplate(t, now);
            } catch (Exception e) {
                log.error("[GvRecurringScheduler] Lỗi khi sinh task cho template id={}: {}", t.getId(), e.getMessage(), e);
            }
        }
    }

    private void processTemplate(GvRecurringTemplate t, Instant now) {
        // Kiểm tra xử lý nợ việc
        if (t.getOverdueAction() == GvOverdueAction.SKIP_UNTIL_DONE) {
            // Kiểm tra xem có task cũ từ template này chưa xong không
            boolean hasUnfinished = taskRepository.findAll().stream()
                    .anyMatch(task -> task.getSourceTemplate() != null
                            && task.getSourceTemplate().getId().equals(t.getId())
                            && task.getStatus() != GvTaskStatus.DONE
                            && task.getStatus() != GvTaskStatus.CANCELLED);

            if (hasUnfinished) {
                log.info("[GvRecurringScheduler] Template id={} có việc kỳ trước chưa hoàn thành, tạm dừng sinh mới (SKIP_UNTIL_DONE)", t.getId());
                advanceNextRun(t, now);
                return;
            }
        }

        // Tính dueDate
        int offset = t.getDeadlineDaysOffset() != null ? t.getDeadlineDaysOffset() : 1;
        Instant dueDate = now.plus(offset, ChronoUnit.DAYS);

        String ym = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = taskRepository.count() + 1;
        String taskCode = String.format("TASK-%s-%04d", ym, count);

        GvTask task = GvTask.builder()
                .code(taskCode)
                .title(t.getTitle())
                .description(t.getDescription())
                .creator(t.getCreator())
                .assignee(t.getDefaultAssignee())
                .department(t.getDepartment())
                .sourceTemplate(t)
                .status(GvTaskStatus.TO_DO)
                .priority(GvTaskPriority.NORMAL)
                .startDate(now)
                .dueDate(dueDate)
                .progress(0)
                .build();

        GvTask saved = taskRepository.save(task);

        // Bắn thông báo giao việc định kỳ
        notificationService.sendNotification(
                t.getDefaultAssignee().getId(),
                saved.getId(),
                GvNotificationType.TASK_ASSIGNED,
                "Công việc định kỳ tự động: " + saved.getCode(),
                "Công việc định kỳ '" + saved.getTitle() + "' đã được hệ thống tự động khởi tạo cho bạn."
        );

        // Cập nhật template
        t.setLastGeneratedAt(now);
        advanceNextRun(t, now);
        templateRepository.save(t);

        log.info("[GvRecurringScheduler] Đã sinh task {} từ template id={}", saved.getCode(), t.getId());
    }

    private void advanceNextRun(GvRecurringTemplate t, Instant now) {
        LocalTime time = LocalTime.of(8, 0);
        if (t.getTimeOfDay() != null && t.getTimeOfDay().contains(":")) {
            String[] parts = t.getTimeOfDay().split(":");
            try {
                time = LocalTime.of(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
            } catch (Exception ignored) {}
        }

        ZonedDateTime zdt = now.atZone(DEFAULT_ZONE).with(time);

        switch (t.getFrequency()) {
            case DAILY -> zdt = zdt.plusDays(1);
            case WEEKLY -> {
                int targetDay = (t.getDayOfWeek() != null && t.getDayOfWeek() >= 1 && t.getDayOfWeek() <= 7) ? t.getDayOfWeek() : 1;
                zdt = zdt.with(DayOfWeek.of(targetDay)).plusWeeks(1);
            }
            case MONTHLY -> {
                int targetDom = (t.getDayOfMonth() != null && t.getDayOfMonth() >= 1 && t.getDayOfMonth() <= 31) ? t.getDayOfMonth() : 1;
                LocalDate d = zdt.toLocalDate().plusMonths(1);
                LocalDate targetDate = d.withDayOfMonth(Math.min(targetDom, d.lengthOfMonth()));
                zdt = targetDate.atTime(time).atZone(DEFAULT_ZONE);
            }
            case QUARTERLY -> {
                int targetDom = (t.getDayOfMonth() != null && t.getDayOfMonth() >= 1 && t.getDayOfMonth() <= 31) ? t.getDayOfMonth() : 1;
                LocalDate d = zdt.toLocalDate().plusMonths(3);
                LocalDate targetDate = d.withDayOfMonth(Math.min(targetDom, d.lengthOfMonth()));
                zdt = targetDate.atTime(time).atZone(DEFAULT_ZONE);
            }
        }

        t.setNextRunAt(zdt.toInstant());
    }
}
