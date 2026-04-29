package vn.hoidanit.springrestwithai.qlkh;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.DeviceRegisterRequest;
import vn.hoidanit.springrestwithai.qlkh.dto.DeviceUnregisterRequest;

/**
 * Quản lý device token FCM của khách hàng.
 * Tất cả route yêu cầu JWT QLKH (Authorization header).
 */
@RestController
@RequestMapping("/api/v1/qlkh/customer")
public class CustomerDeviceController {

    private final NotificationService notificationService;
    private final JwtDecoder jwtDecoder;
    private final CustomerRepository customerRepository;

    public CustomerDeviceController(NotificationService notificationService,
                                    JwtDecoder jwtDecoder,
                                    CustomerRepository customerRepository) {
        this.notificationService = notificationService;
        this.jwtDecoder = jwtDecoder;
        this.customerRepository = customerRepository;
    }

    /**
     * Đăng ký hoặc cập nhật FCM device token.
     * POST /api/v1/qlkh/customer/device/register
     */
    @PostMapping("/device/register")
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DeviceRegisterRequest request) {

        Integer customerId = extractCustomerId(authHeader);
        notificationService.registerDevice(customerId, request.getDeviceToken(), request.getPlatform());
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thiết bị thành công", null));
    }

    /**
     * Huỷ đăng ký FCM device token (dùng khi logout từ client).
     * POST /api/v1/qlkh/customer/device/unregister
     */
    @PostMapping("/device/unregister")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DeviceUnregisterRequest request) {

        Integer customerId = extractCustomerId(authHeader);
        notificationService.unregisterDevice(customerId, request.getDeviceToken());
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thiết bị thành công", null));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private Integer extractCustomerId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        var jwt = jwtDecoder.decode(authHeader.substring(7));
        String digiCode = jwt.getClaimAsString("digiCode");
        return customerRepository.findByDigiCode(digiCode)
                .orElseThrow(() -> new vn.hoidanit.springrestwithai.exception.ResourceNotFoundException(
                        "Khách hàng", "digiCode", digiCode))
                .getCustomerId();
    }
}
