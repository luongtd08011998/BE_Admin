package vn.hoidanit.springrestwithai.feature.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryResponse;
import vn.hoidanit.springrestwithai.feature.category.dto.CreateCategoryRequest;
import vn.hoidanit.springrestwithai.feature.category.dto.UpdateCategoryRequest;

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
}
