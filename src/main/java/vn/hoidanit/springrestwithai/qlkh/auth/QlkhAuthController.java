package vn.hoidanit.springrestwithai.qlkh.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.CustomerLoginRequest;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.CustomerLoginResponse;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.RefreshTokenRequest;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.TokenResponse;

@RestController
@RequestMapping("/api/v1/qlkh")
public class QlkhAuthController {

    private final QlkhAuthService qlkhAuthService;

    public QlkhAuthController(QlkhAuthService qlkhAuthService) {
        this.qlkhAuthService = qlkhAuthService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody CustomerLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công",
                qlkhAuthService.login(request)));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công",
                qlkhAuthService.refresh(request)));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        qlkhAuthService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @GetMapping("/customers/me")
    public ResponseEntity<ApiResponse<CustomerLoginResponse>> getCustomer(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công",
                qlkhAuthService.getCustomer(authHeader)));
    }
}
