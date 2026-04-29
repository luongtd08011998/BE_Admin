package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.dto.MarkReadRequest;
import vn.hoidanit.springrestwithai.qlkh.dto.NotificationResponse;
import vn.hoidanit.springrestwithai.feature.notification.entity.Notification;

import java.util.List;

/**
 * API thông báo của khách hàng.
 * GET  /api/v1/qlkh/customer/notifications       — Lấy danh sách thông báo
 * POST /api/v1/qlkh/customer/notifications/read  — Đánh dấu đã đọc
 */
@RestController
@RequestMapping("/api/v1/qlkh/customer/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtDecoder jwtDecoder;
    private final CustomerRepository customerRepository;
    private final InvoiceNotificationScheduler invoiceNotificationScheduler;

    public NotificationController(NotificationService notificationService,
                                  JwtDecoder jwtDecoder,
                                  CustomerRepository customerRepository,
                                  InvoiceNotificationScheduler invoiceNotificationScheduler) {
        this.notificationService = notificationService;
        this.jwtDecoder = jwtDecoder;
        this.customerRepository = customerRepository;
        this.invoiceNotificationScheduler = invoiceNotificationScheduler;
    }

    /**
     * Lấy toàn bộ thông báo của khách hàng (mới nhất trước).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestHeader("Authorization") String authHeader) {

        Integer customerId = extractCustomerId(authHeader);
        List<NotificationResponse> response = notificationService.getNotifications(customerId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", response));
    }

    /**
     * Đánh dấu đã đọc — ids rỗng/null = đánh dấu tất cả.
     */
    @PostMapping("/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) MarkReadRequest request) {

        Integer customerId = extractCustomerId(authHeader);
        List<Long> ids = request != null ? request.getIds() : null;
        Boolean isSystem = request != null ? request.getIsSystem() : null;
        int updated = notificationService.markAsRead(customerId, ids, isSystem);
        return ResponseEntity.ok(ApiResponse.success(
                "Đánh dấu đã đọc thành công", "Cập nhật " + updated + " thông báo"));
    }

    /**
     * API phụ trợ: Bắn thử Push Notification thực tế qua Firebase tới thiết bị của khách hàng.
     */
    @GetMapping("/test-push")
    public ResponseEntity<ApiResponse<String>> testPushFirebase(
            @RequestHeader("Authorization") String authHeader) {
        
        Integer customerId = extractCustomerId(authHeader);
        // Gọi service lưu và bắn push "Hóa đơn mới"
        notificationService.sendNewInvoiceNotification(customerId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Đã gọi lệnh bắn Push Notification", "Vui lòng kiểm tra điện thoại của bạn"));
    }



    private Integer extractCustomerId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        var jwt = jwtDecoder.decode(authHeader.substring(7));
        String digiCode = jwt.getClaimAsString("digiCode");
        return customerRepository.findByDigiCode(digiCode)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "digiCode", digiCode))
                .getCustomerId();
    }
}
