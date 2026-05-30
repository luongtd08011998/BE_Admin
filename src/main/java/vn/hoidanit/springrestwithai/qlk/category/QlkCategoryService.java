package vn.hoidanit.springrestwithai.qlk.category;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlk.category.dto.CreateQlkCategoryRequest;
import vn.hoidanit.springrestwithai.qlk.category.dto.QlkCategoryResponse;
import vn.hoidanit.springrestwithai.qlk.category.dto.UpdateQlkCategoryRequest;

public interface QlkCategoryService {
    QlkCategoryResponse create(CreateQlkCategoryRequest request);
    QlkCategoryResponse update(Long id, UpdateQlkCategoryRequest request);
    void delete(Long id);
    QlkCategoryResponse getById(Long id);
    ResultPaginationDTO getAll(Pageable pageable);
}
