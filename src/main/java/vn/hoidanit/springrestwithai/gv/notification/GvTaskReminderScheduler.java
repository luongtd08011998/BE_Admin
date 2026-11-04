package vn.hoidanit.springrestwithai.gv.notification;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.gv.task.GvTask;
import vn.hoidanit.springrestwithai.gv.task.GvTaskPriority;
import vn.hoidanit.springrestwithai.gv.task.GvTaskRepository;
import vn.hoidanit.springrestwithai.gv.task.GvTaskStatus;

@Component
public class GvTaskReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(GvTaskReminderScheduler.class);
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withZone(VN_ZONE);

    private final GvTaskRepository taskRepository;
    private final GvNotificationService notificationService;
    private final GvTaskReminderLogRepository reminderLogRepository;

    public GvTaskReminderScheduler(
            GvTaskRepository taskRepository,
            GvNotificationService notificationService,
            GvTaskReminderLogRepository reminderLogRepository) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.reminderLogRepository = reminderLogRepository;
    }

    /**
     * Chạy mỗi 30 phút một lần để quét công việc sắp đến hạn hoặc quá hạn.
     * Áp dụng quy chế nhắc việc theo cấp độ ưu tiên và khung giờ làm việc (7:30 - 17:30).
     */
    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void checkDeadlinesAndRemind() {
        ZonedDateTime nowVN = ZonedDateTime.now(VN_ZONE);
        Instant now = nowVN.toInstant();

        int hour = nowVN.getHour();
        int minute = nowVN.getMinute();
        double timeFloat = hour + minute / 60.0;
        // Giờ làm việc hành chính cấp nước: 7h30 sáng đến 17h30 chiều
        boolean isBusinessHours = (timeFloat >= 7.5 && timeFloat <= 17.5);

        List<GvTaskStatus> completedStatuses = List.of(GvTaskStatus.DONE, GvTaskStatus.CANCELLED);
        List<GvTask> activeTasks = taskRepository.findByStatusNotIn(completedStatuses);

        int remindedCount = 0;
        int overdueAlertCount = 0;

        for (GvTask t : activeTasks) {
            if (t.getDueDate() == null || t.getAssignee() == null) {
                continue;
            }

            Duration diff = Duration.between(now, t.getDueDate());
            Long assigneeId = t.getAssignee().getId();
            Long managerId = (t.getDepartment() != null && t.getDepartment().getManager() != null)
                    ? t.getDepartment().getManager().getId() : null;
            Long creatorId = t.getCreator() != null ? t.getCreator().getId() : null;

            // ─── 1. CÔNG VIỆC CHƯA TỚI HẠN (SẮP ĐẾN HẠN) ───
            if (diff.getSeconds() > 0) {
                long minutesLeft = diff.toMinutes();
                long hoursLeft = diff.toHours();
                GvTaskPriority prio = t.getPriority() != null ? t.getPriority() : GvTaskPriority.NORMAL;

                // 🔴 KHẨN CẤP (URGENT): Nhắc 24/7 (kể cả ngoài giờ làm việc)
                if (prio == GvTaskPriority.URGENT) {
                    if (minutesLeft <= 60) {
                        if (sendReminderIfNotSent(t, assigneeId, "URGENT_REMINDER_1H", GvNotificationType.DEADLINE_REMINDER,
                                "🔥 [KHẨN CẤP] Sắp đến hạn: " + t.getCode(),
                                "Công việc khẩn cấp '" + t.getTitle() + "' chỉ còn " + minutesLeft + " phút nữa là đến hạn (" + formatTime(t.getDueDate()) + "). Vui lòng hoàn tất gấp!")) {
                            remindedCount++;
                        }
                    }
                    // Kiểm tra việc khẩn cấp tạo quá 1 tiếng mà vẫn chưa tiếp nhận (TO_DO)
                    if (t.getCreatedAt() != null && t.getStatus() == GvTaskStatus.TO_DO) {
                        long createdMinutes = Duration.between(t.getCreatedAt(), now).toMinutes();
                        if (createdMinutes >= 60) {
                            if (sendReminderIfNotSent(t, assigneeId, "URGENT_STALLED_1H", GvNotificationType.DEADLINE_REMINDER,
                                    "🔥 [KHẨN CẤP] Chưa tiếp nhận: " + t.getCode(),
                                    "Công việc khẩn cấp '" + t.getTitle() + "' đã giao hơn 1 tiếng nhưng chưa được tiếp nhận xử lý!")) {
                                remindedCount++;
                            }
                        }
                    }
                } else if (isBusinessHours) {
                    // 🟠 CAO (HIGH - Công việc chính): 1 ngày trước + 2 giờ trước
                    if (prio == GvTaskPriority.HIGH) {
                        if (hoursLeft <= 2) {
                            String timeStr = minutesLeft > 60 ? (hoursLeft + " giờ") : (minutesLeft + " phút");
                            if (sendReminderIfNotSent(t, assigneeId, "HIGH_REMINDER_2H", GvNotificationType.DEADLINE_REMINDER,
                                    "🟠 Nhắc hạn chót: " + t.getCode(),
                                    "Công việc trọng tâm '" + t.getTitle() + "' chỉ còn " + timeStr + " nữa đến hạn (" + formatTime(t.getDueDate()) + "). Vui lòng hoàn thành đúng tiến độ.")) {
                                remindedCount++;
                            }
                        } else if (hoursLeft <= 24) {
                            if (sendReminderIfNotSent(t, assigneeId, "HIGH_REMINDER_1D", GvNotificationType.DEADLINE_REMINDER,
                                    "🟠 Nhắc hạn (Còn 1 ngày): " + t.getCode(),
                                    "Công việc trọng tâm '" + t.getTitle() + "' có hạn chót vào ngày mai (" + formatTime(t.getDueDate()) + ").")) {
                                remindedCount++;
                            }
                        }
                    }
                    // 🔵 TRUNG BÌNH (NORMAL): 2 ngày trước + 4 giờ trước
                    else if (prio == GvTaskPriority.NORMAL) {
                        if (hoursLeft <= 4) {
                            if (sendReminderIfNotSent(t, assigneeId, "NORMAL_REMINDER_4H", GvNotificationType.DEADLINE_REMINDER,
                                    "🔵 Nhắc hạn chót: " + t.getCode(),
                                    "Công việc '" + t.getTitle() + "' chỉ còn " + hoursLeft + " giờ nữa đến hạn (" + formatTime(t.getDueDate()) + "). Vui lòng kiểm tra tiến độ.")) {
                                remindedCount++;
                            }
                        } else if (hoursLeft <= 48) {
                            if (sendReminderIfNotSent(t, assigneeId, "NORMAL_REMINDER_2D", GvNotificationType.DEADLINE_REMINDER,
                                    "🔵 Nhắc hạn (Còn 2 ngày): " + t.getCode(),
                                    "Công việc '" + t.getTitle() + "' có hạn chót sau 2 ngày (" + formatTime(t.getDueDate()) + ").")) {
                                remindedCount++;
                            }
                        }
                    }
                    // ⚪ THẤP (LOW - Công việc phụ): 3 ngày trước + 1 ngày trước
                    else if (prio == GvTaskPriority.LOW) {
                        if (hoursLeft <= 24) {
                            if (sendReminderIfNotSent(t, assigneeId, "LOW_REMINDER_1D", GvNotificationType.DEADLINE_REMINDER,
                                    "⚪ Nhắc hạn: " + t.getCode(),
                                    "Công việc hỗ trợ '" + t.getTitle() + "' sắp đến hạn (" + formatTime(t.getDueDate()) + ").")) {
                                remindedCount++;
                            }
                        } else if (hoursLeft <= 72) {
                            if (sendReminderIfNotSent(t, assigneeId, "LOW_REMINDER_3D", GvNotificationType.DEADLINE_REMINDER,
                                    "⚪ Nhắc hạn (Còn 3 ngày): " + t.getCode(),
                                    "Công việc hỗ trợ '" + t.getTitle() + "' có hạn chót sau 3 ngày (" + formatTime(t.getDueDate()) + ").")) {
                                remindedCount++;
                            }
                        }
                    }
                }
            }
            // ─── 2. CÔNG VIỆC ĐÃ QUÁ HẠN ───
            else {
                long overdueMinutes = Math.abs(diff.toMinutes());
                long overdueHours = Math.abs(diff.toHours());

                if (isBusinessHours || t.getPriority() == GvTaskPriority.URGENT) {
                    // Mốc 1: Quá hạn >= 1 giờ -> Cảnh báo nhân viên + báo trưởng phòng / người giao
                    if (overdueMinutes >= 60) {
                        if (sendReminderIfNotSent(t, assigneeId, "OVERDUE_1H_ASSIGNEE", GvNotificationType.OVERDUE_ALERT,
                                "⚠️ CẢNH BÁO QUÁ HẠN: " + t.getCode(),
                                "Công việc '" + t.getTitle() + "' đã quá hạn quy định (" + formatTime(t.getDueDate()) + "). Vui lòng cập nhật trạng thái hoặc báo cáo lý do trễ.")) {
                            overdueAlertCount++;
                        }

                        String mgrContent = "Nhân viên " + t.getAssignee().getName() + " chưa hoàn thành công việc '" + t.getTitle() + "' (Hạn: " + formatTime(t.getDueDate()) + ").";
                        if (managerId != null && !managerId.equals(assigneeId)) {
                            sendReminderIfNotSent(t, managerId, "OVERDUE_1H_MANAGER", GvNotificationType.OVERDUE_ALERT,
                                    "⚠️ TIẾN ĐỘ: Nhân viên trễ hạn: " + t.getCode(), mgrContent);
                        }
                        if (creatorId != null && !creatorId.equals(assigneeId) && !creatorId.equals(managerId)) {
                            sendReminderIfNotSent(t, creatorId, "OVERDUE_1H_CREATOR", GvNotificationType.OVERDUE_ALERT,
                                    "⚠️ TIẾN ĐỘ: Công việc bạn giao trễ hạn: " + t.getCode(), mgrContent);
                        }
                    }

                    // Mốc 2: Quá hạn >= 24 giờ (1 ngày) -> Cảnh báo trừ KPI
                    if (overdueHours >= 24) {
                        if (sendReminderIfNotSent(t, assigneeId, "OVERDUE_1D_ASSIGNEE", GvNotificationType.OVERDUE_ALERT,
                                "🚨 CẢNH BÁO: Quá hạn 1 ngày: " + t.getCode(),
                                "Công việc '" + t.getTitle() + "' đã trễ hạn 1 ngày. Điểm KPI tiến độ sẽ bị giảm trừ theo quy chế công ty.")) {
                            overdueAlertCount++;
                        }

                        String dContent = "Công việc '" + t.getTitle() + "' của " + t.getAssignee().getName() + " đã quá hạn 1 ngày. Đề nghị lãnh đạo phòng đôn đốc xử lý!";
                        if (managerId != null && !managerId.equals(assigneeId)) {
                            sendReminderIfNotSent(t, managerId, "OVERDUE_1D_MANAGER", GvNotificationType.OVERDUE_ALERT,
                                    "🚨 ĐÔN ĐỐC TIẾN ĐỘ (Trễ 1 ngày): " + t.getCode(), dContent);
                        }
                        if (creatorId != null && !creatorId.equals(assigneeId) && !creatorId.equals(managerId)) {
                            sendReminderIfNotSent(t, creatorId, "OVERDUE_1D_CREATOR", GvNotificationType.OVERDUE_ALERT,
                                    "🚨 ĐÔN ĐỐC TIẾN ĐỘ (Trễ 1 ngày): " + t.getCode(), dContent);
                        }
                    }

                    // Mốc 3: Quá hạn >= 72 giờ (3 ngày) -> Báo cáo vi phạm nghiêm trọng
                    if (overdueHours >= 72) {
                        if (sendReminderIfNotSent(t, assigneeId, "OVERDUE_3D_ASSIGNEE", GvNotificationType.OVERDUE_ALERT,
                                "🔥 VI PHẠM TIẾN ĐỘ NGHIÊM TRỌNG: " + t.getCode(),
                                "Công việc '" + t.getTitle() + "' đã trễ hạn hơn 3 ngày. Hệ thống ghi nhận điểm tiến độ = 0 và báo cáo Ban Giám Đốc.")) {
                            overdueAlertCount++;
                        }

                        String escContent = "Công việc '" + t.getTitle() + "' của " + t.getAssignee().getName() + " trễ hạn hơn 3 ngày!";
                        if (managerId != null && !managerId.equals(assigneeId)) {
                            sendReminderIfNotSent(t, managerId, "OVERDUE_3D_MANAGER", GvNotificationType.OVERDUE_ALERT,
                                    "🔥 BÁO CÁO VI PHẠM (Trễ 3 ngày): " + t.getCode(), escContent);
                        }
                        if (creatorId != null && !creatorId.equals(assigneeId) && !creatorId.equals(managerId)) {
                            sendReminderIfNotSent(t, creatorId, "OVERDUE_3D_CREATOR", GvNotificationType.OVERDUE_ALERT,
                                    "🔥 BÁO CÁO VI PHẠM (Trễ 3 ngày): " + t.getCode(), escContent);
                        }
                    }
                }
            }
        }

        log.info("[GvTaskReminderScheduler] Đã quét {} việc active: gửi {} nhắc nhở sắp hạn, {} cảnh báo quá hạn",
                activeTasks.size(), remindedCount, overdueAlertCount);
    }

    private boolean sendReminderIfNotSent(GvTask task, Long recipientId, String reminderType,
                                          GvNotificationType notiType, String title, String content) {
        if (recipientId == null) return false;
        if (reminderLogRepository.existsByTaskIdAndRecipientIdAndReminderType(task.getId(), recipientId, reminderType)) {
            return false;
        }

        notificationService.sendNotification(recipientId, task.getId(), notiType, title, content);

        GvTaskReminderLog logRecord = GvTaskReminderLog.builder()
                .taskId(task.getId())
                .recipientId(recipientId)
                .reminderType(reminderType)
                .sentAt(Instant.now())
                .build();
        reminderLogRepository.save(logRecord);
        return true;
    }

    private String formatTime(Instant instant) {
        if (instant == null) return "";
        return TIME_FORMATTER.format(instant);
    }
}
