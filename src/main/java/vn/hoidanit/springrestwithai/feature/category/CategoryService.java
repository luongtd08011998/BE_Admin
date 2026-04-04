package vn.hoidanit.springrestwithai.feature.category;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.category.dto.CategoryResponse;
import vn.hoidanit.springrestwithai.feature.category.dto.CreateCategoryRequest;
import vn.hoidanit.springrestwithai.feature.category.dto.UpdateCategoryRequest;

public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse update(UpdateCategoryRequest request);

    CategoryResponse getById(Long id);

    ResultPaginationDTO getAll(Pageable pageable);

    void delete(Long id);
}
