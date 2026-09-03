package vn.hoidanit.springrestwithai.feature.appconfig;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.AppConfigFilterRequest;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.AppConfigResponse;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.CreateAppConfigRequest;
import vn.hoidanit.springrestwithai.feature.appconfig.dto.UpdateAppConfigRequest;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/app-configs")
public class AppConfigController {

    private final AppConfigService appConfigService;

    public AppConfigController(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<AppConfigResponse>> getLatestConfig(
            @RequestParam String platform) {
        AppConfigResponse response = appConfigService.getLatestConfig(platform);
        return ResponseEntity.ok(ApiResponse.success("Lấy cấu hình ứng dụng thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> filter(
            @ParameterObject AppConfigFilterRequest filter,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = appConfigService.filter(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cấu hình thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppConfigResponse>> getById(@PathVariable Long id) {
        AppConfigResponse response = appConfigService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết cấu hình thành công", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppConfigResponse>> create(
            @Valid @RequestBody CreateAppConfigRequest request) {
        AppConfigResponse response = appConfigService.create(request);
        URI location = URI.create("/api/v1/app-configs/" + response.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.created("Tạo cấu hình thành công", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<AppConfigResponse>> update(
            @Valid @RequestBody UpdateAppConfigRequest request) {
        AppConfigResponse response = appConfigService.update(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        appConfigService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa cấu hình thành công", null));
    }
}
