package vn.hoidanit.springrestwithai.qlk.material;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlk.material.dto.CreateMaterialRequest;
import vn.hoidanit.springrestwithai.qlk.material.dto.MaterialResponse;
import vn.hoidanit.springrestwithai.qlk.material.dto.UpdateMaterialRequest;

public interface MaterialService {
    MaterialResponse create(CreateMaterialRequest request);
    MaterialResponse create(CreateMaterialRequest request, Long userId);
    MaterialResponse update(Long id, UpdateMaterialRequest request);
    void delete(Long id);
    MaterialResponse getById(Long id);
    ResultPaginationDTO getAll(Pageable pageable);
}
