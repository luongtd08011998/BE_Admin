package vn.hoidanit.springrestwithai.qlkh.device;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.qlkh.device.dto.DeviceRegisterRequest;
import vn.hoidanit.springrestwithai.qlkh.device.dto.DeviceUnregisterRequest;

@RestController
@RequestMapping("/api/v1/qlkh/customer")
public class CustomerDeviceController {

    private final CustomerDeviceService customerDeviceService;

    public CustomerDeviceController(CustomerDeviceService customerDeviceService) {
        this.customerDeviceService = customerDeviceService;
    }

    @PostMapping("/device/register")
    public ResponseEntity<ApiResponse<Void>> registerDevice(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DeviceRegisterRequest request) {
        customerDeviceService.registerDevice(authHeader, request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thiết bị thành công", null));
    }

    @PostMapping("/device/unregister")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DeviceUnregisterRequest request) {
        customerDeviceService.unregisterDevice(authHeader, request);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thiết bị thành công", null));
    }
}
