package vn.hoidanit.springrestwithai.qlk.warehouse;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.AssignWarehouseUsersRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.CreateWarehouseRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.UpdateWarehouseRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.WarehouseResponse;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;

import java.util.List;

public interface WarehouseService {
    WarehouseResponse create(CreateWarehouseRequest request);
    WarehouseResponse update(Long id, UpdateWarehouseRequest request);
    WarehouseResponse updateAlertThreshold(Long id, Integer threshold);
    void delete(Long id);
    WarehouseResponse getById(Long id);
    ResultPaginationDTO getAll(Pageable pageable);
    List<WarehouseResponse> getAllActiveWarehouses();
    void assignUsers(Long warehouseId, AssignWarehouseUsersRequest request);
    List<UserResponse> getAssignedUsers(Long warehouseId);
}
