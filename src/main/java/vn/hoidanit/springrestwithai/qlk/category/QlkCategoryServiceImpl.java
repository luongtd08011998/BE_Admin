package vn.hoidanit.springrestwithai.qlk.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlk.category.dto.CreateQlkCategoryRequest;
import vn.hoidanit.springrestwithai.qlk.category.dto.QlkCategoryResponse;
import vn.hoidanit.springrestwithai.qlk.category.dto.UpdateQlkCategoryRequest;
import vn.hoidanit.springrestwithai.qlk.warehouse.Warehouse;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseRepository;
import vn.hoidanit.springrestwithai.security.JwtUtil;

@Service
@Transactional
public class QlkCategoryServiceImpl implements QlkCategoryService {

    private final QlkCategoryRepository qlkCategoryRepository;
    private final WarehouseRepository warehouseRepository;

    public QlkCategoryServiceImpl(QlkCategoryRepository qlkCategoryRepository,
                                   WarehouseRepository warehouseRepository) {
        this.qlkCategoryRepository = qlkCategoryRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public QlkCategoryResponse create(CreateQlkCategoryRequest request) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        Warehouse warehouse = resolveWarehouse(warehouseId);

        if (qlkCategoryRepository.existsByNameAndWarehouseId(request.name(), warehouseId)) {
            throw new DuplicateResourceException("Danh mục vật tư", "name", request.name());
        }

        QlkCategory category = QlkCategory.builder()
                .name(request.name())
                .description(request.description())
                .warehouse(warehouse)
                .build();

        return QlkCategoryResponse.from(qlkCategoryRepository.save(category));
    }

    @Override
    public QlkCategoryResponse update(Long id, UpdateQlkCategoryRequest request) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        QlkCategory category = getCategoryInCurrentWarehouse(id, warehouseId);

        if (qlkCategoryRepository.existsByNameAndWarehouseIdAndIdNot(request.name(), warehouseId, id)) {
            throw new DuplicateResourceException("Danh mục vật tư", "name", request.name());
        }

        category.setName(request.name());
        category.setDescription(request.description());

        return QlkCategoryResponse.from(qlkCategoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        QlkCategory category = getCategoryInCurrentWarehouse(id, warehouseId);
        qlkCategoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public QlkCategoryResponse getById(Long id) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        return QlkCategoryResponse.from(getCategoryInCurrentWarehouse(id, warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(Pageable pageable) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        Page<QlkCategory> page = qlkCategoryRepository.findByWarehouseId(warehouseId, pageable);
        return ResultPaginationDTO.fromPage(page.map(QlkCategoryResponse::from));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Warehouse resolveWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kho", "id", warehouseId.toString()));
    }

    /**
     * Lấy danh mục theo id và đảm bảo nó thuộc về kho hiện tại của user.
     */
    private QlkCategory getCategoryInCurrentWarehouse(Long categoryId, Long warehouseId) {
        QlkCategory category = qlkCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục vật tư", "id", categoryId.toString()));

        if (!category.getWarehouse().getId().equals(warehouseId)) {
            // Ẩn thông tin, trả 404 để tránh lộ dữ liệu kho khác
            throw new ResourceNotFoundException("Danh mục vật tư", "id", categoryId.toString());
        }

        return category;
    }
}
