package vn.hoidanit.springrestwithai.qlkh.notification.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationAdminFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationAdminResponse;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationStatisticsResponse;

@RestController
@RequestMapping("/api/v1/admin/notifications")
public class NotificationAdminController {

    private final NotificationAdminService notificationAdminService;

    public NotificationAdminController(NotificationAdminService notificationAdminService) {
        this.notificationAdminService = notificationAdminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getNotifications(
            @ParameterObject NotificationAdminFilterRequest filter,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = notificationAdminService.getNotifications(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", result));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<NotificationStatisticsResponse>> getStatistics() {
        NotificationStatisticsResponse result = notificationAdminService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê thông báo thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationAdminResponse>> getNotification(@PathVariable Long id) {
        NotificationAdminResponse result = notificationAdminService.getNotification(id);
        if (result == null) {
            return ResponseEntity.ok(ApiResponse.error("Không tìm thấy thông báo với id=" + id));
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thông báo thành công", result));
    }

    @PostMapping("/{id}/resend")
    public ResponseEntity<ApiResponse<NotificationAdminResponse>> resendNotification(@PathVariable Long id) {
        NotificationAdminResponse result = notificationAdminService.resendNotification(id);
        if (result == null) {
            return ResponseEntity.ok(ApiResponse.error("Không tìm thấy thông báo với id=" + id));
        }
        return ResponseEntity.ok(ApiResponse.success("Gửi lại thông báo thành công", result));
    }
}
