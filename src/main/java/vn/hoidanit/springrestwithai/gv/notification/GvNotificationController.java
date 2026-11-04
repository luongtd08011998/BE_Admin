package vn.hoidanit.springrestwithai.gv.notification;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.gv.notification.dto.GvDeviceTokenRequest;
import vn.hoidanit.springrestwithai.gv.notification.dto.GvNotificationResponse;
import vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil;

@RestController
@RequestMapping("/api/v1/gv/notifications")
public class GvNotificationController {

    private final GvNotificationService notificationService;
    private final GvTaskReminderScheduler reminderScheduler;

    public GvNotificationController(
            GvNotificationService notificationService,
            GvTaskReminderScheduler reminderScheduler) {
        this.notificationService = notificationService;
        this.reminderScheduler = reminderScheduler;
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<ApiResponse<Page<GvNotificationResponse>>> getMyNotifications(Pageable pageable) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(userId, pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{id}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/register-device")
    public ResponseEntity<ApiResponse<Void>> registerDevice(@Valid @RequestBody GvDeviceTokenRequest request) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        notificationService.registerDevice(userId, request.getDeviceToken(), request.getPlatform());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/unregister-device")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(@Valid @RequestBody GvDeviceTokenRequest request) {
        Long userId = GvSecurityUtil.getRequiredCurrentUserId();
        notificationService.unregisterDevice(userId, request.getDeviceToken());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/trigger-reminders")
    public ResponseEntity<ApiResponse<String>> triggerReminders() {
        reminderScheduler.checkDeadlinesAndRemind();
        return ResponseEntity.ok(ApiResponse.success("Đã kích hoạt quét kiểm tra hạn công việc thành công"));
    }
}
