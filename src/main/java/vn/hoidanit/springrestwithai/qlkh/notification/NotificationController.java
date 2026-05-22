package vn.hoidanit.springrestwithai.qlkh.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.notification.dto.MarkReadRequest;
import vn.hoidanit.springrestwithai.qlkh.notification.dto.NotificationResponse;

import java.util.List;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;

/**
 * API thông báo của khách hàng.
 * GET  /api/v1/qlkh/customer/notifications            — Lấy danh sách thông báo
 * GET  /api/v1/qlkh/customer/notifications/unread-count — Số chưa đọc
 * POST /api/v1/qlkh/customer/notifications/read       — Đánh dấu đã đọc
 */
@RestController
@RequestMapping("/api/v1/qlkh/customer/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtDecoder jwtDecoder;
    private final CustomerRepository customerRepository;

    public NotificationController(NotificationService notificationService,
                                  JwtDecoder jwtDecoder,
                                  CustomerRepository customerRepository) {
        this.notificationService = notificationService;
        this.jwtDecoder = jwtDecoder;
        this.customerRepository = customerRepository;
    }

    /**
     * Lấy toàn bộ thông báo của khách hàng (mới nhất trước).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String excludeType) {

        Integer customerId = extractCustomerId(authHeader);
        List<NotificationResponse> response = notificationService.getNotifications(customerId, type, excludeType);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", response));
    }

    /**
     * Lấy số lượng thông báo chưa đọc của khách hàng.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String excludeType) {

        Integer customerId = extractCustomerId(authHeader);
        long count = notificationService.getUnreadCount(customerId, type, excludeType);
        return ResponseEntity.ok(ApiResponse.success("Lấy số lượng chưa đọc thành công", count));
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
