package vn.hoidanit.springrestwithai.feature.category;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryResponse;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryTreeResponseDTO;
import vn.hoidanit.springrestwithai.feature.category.dto.CreateCategoryRequest;
import vn.hoidanit.springrestwithai.feature.category.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse update(UpdateCategoryRequest request);

    CategoryResponse getById(Long id);

    ResultPaginationDTO getAll(Pageable pageable);

    void delete(Long id);

    List<CategoryResponse> searchByName(String keyword);

    List<CategoryResponse> getByParentId(Long parentId);

    CategoryResponse getBySlug(String slug);

    CategoryTreeResponseDTO getCategoryTree(Long id);

    List<CategoryTreeResponseDTO> getAllAsTree(String keyword);
}
