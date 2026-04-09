package vn.hoidanit.springrestwithai.feature.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryResponse;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryTreeResponseDTO;
import vn.hoidanit.springrestwithai.feature.category.dto.CreateCategoryRequest;
import vn.hoidanit.springrestwithai.feature.category.dto.UpdateCategoryRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateResourceException("Danh mục", "slug", request.slug());
        }

        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setActive(request.active() != null ? request.active() : 1);

        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha", "id", request.parentId()));
            category.setParent(parent);
        }

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.id()));

        if (categoryRepository.existsBySlugAndIdNot(request.slug(), request.id())) {
            throw new DuplicateResourceException("Danh mục", "slug", request.slug());
        }

        category.setName(request.name());
        category.setSlug(request.slug());
        if (request.active() != null) {
            category.setActive(request.active());
        }

        if (request.parentId() != null) {
            if (request.parentId().equals(request.id())) {
                throw new IllegalArgumentException("Danh mục không thể là cha của chính nó");
            }
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha", "id", request.parentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        return CategoryResponse.fromEntity(category);
    }

    @Override
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<CategoryResponse> pageResult = categoryRepository.findAll(pageable)
                .map(CategoryResponse::fromEntity);
        return ResultPaginationDTO.fromPage(pageResult);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Danh mục", "id", id);
        }
        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalStateException("Không thể xóa danh mục đang có danh mục con");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryResponse> searchByName(String keyword) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CategoryResponse> getByParentId(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Danh mục cha", "id", parentId);
        }
        return categoryRepository.findByParentId(parentId)
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "slug", slug));
        return CategoryResponse.fromEntity(category);
    }

    @Override
    public CategoryTreeResponseDTO getCategoryTree(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        return buildTree(category);
    }

    @Override
    public List<CategoryTreeResponseDTO> getAllAsTree(String keyword) {
        List<Category> all = (keyword != null && !keyword.isBlank())
                ? categoryRepository.findByNameContainingIgnoreCase(keyword)
                : categoryRepository.findAll();

        Map<Long, CategoryTreeResponseDTO> dtoMap = all.stream()
                .map(c -> new CategoryTreeResponseDTO(
                        c.getId(), c.getName(), c.getSlug(),
                        c.getCreatedAt(), c.getUpdatedAt()))
                .collect(Collectors.toMap(CategoryTreeResponseDTO::getId, Function.identity()));

        List<CategoryTreeResponseDTO> roots = new ArrayList<>();

        for (Category c : all) {
            CategoryTreeResponseDTO dto = dtoMap.get(c.getId());
            if (c.getParent() == null) {
                roots.add(dto);
            } else {
                CategoryTreeResponseDTO parentDto = dtoMap.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                } else {
                    // parent không nằm trong kết quả (do filter keyword) → treat as root
                    roots.add(dto);
                }
            }
        }

        return roots;
    }

    private CategoryTreeResponseDTO buildTree(Category category) {
        CategoryTreeResponseDTO dto = new CategoryTreeResponseDTO(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );

        if (category.getParent() != null) {
            Category parent = category.getParent();
            dto.setParent(new CategoryTreeResponseDTO(
                    parent.getId(),
                    parent.getName(),
                    parent.getSlug(),
                    parent.getCreatedAt(),
                    parent.getUpdatedAt()
            ));
        }

        List<CategoryTreeResponseDTO> childDTOs = categoryRepository.findByParentId(category.getId())
                .stream()
                .map(this::buildTree)
                .toList();
        dto.setChildren(childDTOs);

        return dto;
    }
}
