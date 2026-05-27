package vn.hoidanit.springrestwithai.qlkh.notification.admin;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.CustomerDeviceStatusFilterRequest;

@RestController
@RequestMapping("/api/v1/admin/customer-devices")
public class CustomerDeviceAdminController {

    private final CustomerDeviceAdminService customerDeviceAdminService;

    public CustomerDeviceAdminController(CustomerDeviceAdminService customerDeviceAdminService) {
        this.customerDeviceAdminService = customerDeviceAdminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getCustomerDeviceStatus(
            @ParameterObject CustomerDeviceStatusFilterRequest filter,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = customerDeviceAdminService.getCustomerDeviceStatus(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách trạng thái thiết bị khách hàng thành công", result));
    }
}
