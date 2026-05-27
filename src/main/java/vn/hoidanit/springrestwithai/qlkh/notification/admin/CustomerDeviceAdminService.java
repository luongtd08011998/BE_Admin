package vn.hoidanit.springrestwithai.qlkh.notification.admin;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.CustomerDeviceStatusFilterRequest;

public interface CustomerDeviceAdminService {

    ResultPaginationDTO getCustomerDeviceStatus(CustomerDeviceStatusFilterRequest filter, Pageable pageable);
}
