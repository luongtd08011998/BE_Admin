package vn.hoidanit.springrestwithai.qlk.warehouse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.AssignWarehouseUsersRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.CreateWarehouseRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.UpdateWarehouseRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.WarehouseResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseUserRepository warehouseUserRepository;
    private final UserRepository userRepository;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository,
                                WarehouseUserRepository warehouseUserRepository,
                                UserRepository userRepository) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseUserRepository = warehouseUserRepository;
        this.userRepository = userRepository;
    }

    @Override
    public WarehouseResponse create(CreateWarehouseRequest request) {
        if (warehouseRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Kho", "code", request.code());
        }

        Warehouse warehouse = Warehouse.builder()
                .code(request.code())
                .name(request.name())
                .address(request.address())
                .description(request.description())
                .status(request.status() != null ? request.status() : WarehouseStatus.HOAT_DONG)
                .build();

        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseResponse update(Long id, UpdateWarehouseRequest request) {
        Warehouse warehouse = getWarehouseById(id);

        warehouse.setName(request.name());
        warehouse.setAddress(request.address());
        warehouse.setDescription(request.description());
        if (request.status() != null) {
            warehouse.setStatus(request.status());
        }

        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseResponse updateAlertThreshold(Long id, Integer threshold) {
        Warehouse warehouse = getWarehouseById(id);
        warehouse.setAlertThreshold(threshold);
        return WarehouseResponse.from(warehouseRepository.save(warehouse));
    }

    @Override
    public void delete(Long id) {
        Warehouse warehouse = getWarehouseById(id);
        
        // Remove assigned users first due to foreign key
        warehouseUserRepository.deleteByWarehouseId(id);
        
        // Then delete the warehouse
        warehouseRepository.delete(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getById(Long id) {
        return WarehouseResponse.from(getWarehouseById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<Warehouse> page = warehouseRepository.findAll(pageable);
        return ResultPaginationDTO.fromPage(page.map(WarehouseResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllActiveWarehouses() {
        // Find all warehouses and filter by status
        return warehouseRepository.findAll().stream()
                .filter(w -> w.getStatus() == WarehouseStatus.HOAT_DONG)
                .map(WarehouseResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void assignUsers(Long warehouseId, AssignWarehouseUsersRequest request) {
        Warehouse warehouse = getWarehouseById(warehouseId);

        // Current users
        List<WarehouseUser> currentAssignments = warehouseUserRepository.findByWarehouseId(warehouseId);
        
        // Remove existing assignments that are not in the new list
        currentAssignments.stream()
                .filter(wu -> !request.userIds().contains(wu.getUser().getId()))
                .forEach(warehouseUserRepository::delete);

        // Add new assignments
        List<Long> currentUserIds = currentAssignments.stream()
                .map(wu -> wu.getUser().getId())
                .toList();

        for (Long userId : request.userIds()) {
            if (!currentUserIds.contains(userId)) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId.toString()));
                
                WarehouseUser warehouseUser = WarehouseUser.builder()
                        .warehouse(warehouse)
                        .user(user)
                        .build();
                warehouseUserRepository.save(warehouseUser);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAssignedUsers(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Kho", "id", warehouseId.toString());
        }
        
        return warehouseUserRepository.findByWarehouseId(warehouseId).stream()
                .map(wu -> UserResponse.fromEntity(wu.getUser()))
                .collect(Collectors.toList());
    }

    private Warehouse getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kho", "id", id.toString()));
    }
}
