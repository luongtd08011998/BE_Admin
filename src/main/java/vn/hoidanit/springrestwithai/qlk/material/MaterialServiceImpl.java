package vn.hoidanit.springrestwithai.qlk.material;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.exception.BadRequestException;
import vn.hoidanit.springrestwithai.security.JwtUtil;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.qlk.category.QlkCategory;
import vn.hoidanit.springrestwithai.qlk.category.QlkCategoryRepository;
import vn.hoidanit.springrestwithai.qlk.material.dto.CreateMaterialRequest;
import vn.hoidanit.springrestwithai.qlk.material.dto.MaterialResponse;
import vn.hoidanit.springrestwithai.qlk.material.dto.UpdateMaterialRequest;

@Service
@Transactional
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final QlkCategoryRepository qlkCategoryRepository;
    private final UserRepository userRepository;

    public MaterialServiceImpl(MaterialRepository materialRepository, 
                               QlkCategoryRepository qlkCategoryRepository,
                               UserRepository userRepository) {
        this.materialRepository = materialRepository;
        this.qlkCategoryRepository = qlkCategoryRepository;
        this.userRepository = userRepository;
    }

    public MaterialResponse create(CreateMaterialRequest request, Long userId) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        
        if (materialRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Vật tư", "code", request.code());
        }

        QlkCategory category = null;
        if (request.categoryId() != null) {
            category = qlkCategoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục vật tư", "id", request.categoryId().toString()));
            
            if (!category.getWarehouse().getId().equals(warehouseId)) {
                throw new BadRequestException("Danh mục không thuộc kho làm việc hiện tại");
            }
        }

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        Material material = Material.builder()
                .code(request.code())
                .name(request.name())
                .specification(request.specification())
                .unit(request.unit())
                .unitPrice(request.unitPrice())
                .minStock(request.minStock())
                .barcode(request.barcode())
                .imageUrl(request.imageUrl())
                .category(category)
                .createdBy(user)
                .status(MaterialStatus.DANG_SU_DUNG)
                .build();

        return MaterialResponse.from(materialRepository.save(material));
    }

    @Override
    public MaterialResponse create(CreateMaterialRequest request) {
        return create(request, null);
    }

    @Override
    public MaterialResponse update(Long id, UpdateMaterialRequest request) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        Material material = getMaterialByIdAndWarehouseId(id, warehouseId);

        QlkCategory category = null;
        if (request.categoryId() != null) {
            category = qlkCategoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục vật tư", "id", request.categoryId().toString()));
                    
            if (!category.getWarehouse().getId().equals(warehouseId)) {
                throw new BadRequestException("Danh mục không thuộc kho làm việc hiện tại");
            }
        }

        material.setName(request.name());
        material.setSpecification(request.specification());
        material.setUnit(request.unit());
        material.setUnitPrice(request.unitPrice());
        material.setMinStock(request.minStock());
        material.setBarcode(request.barcode());
        material.setImageUrl(request.imageUrl());
        material.setCategory(category);
        if (request.status() != null) {
            material.setStatus(request.status());
        }

        return MaterialResponse.from(materialRepository.save(material));
    }

    @Override
    public void delete(Long id) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        Material material = getMaterialByIdAndWarehouseId(id, warehouseId);
        materialRepository.delete(material);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponse getById(Long id) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        return MaterialResponse.from(getMaterialByIdAndWarehouseId(id, warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(Pageable pageable) {
        Long warehouseId = JwtUtil.extractWarehouseId();
        Page<Material> page = materialRepository.findByCategoryWarehouseId(warehouseId, pageable);
        return ResultPaginationDTO.fromPage(page.map(MaterialResponse::from));
    }

    private Material getMaterialByIdAndWarehouseId(Long id, Long warehouseId) {
        return materialRepository.findByIdAndCategoryWarehouseId(id, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Vật tư", "id", id.toString()));
    }
}
