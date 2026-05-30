package vn.hoidanit.springrestwithai.qlk.supplier;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.CreateSupplierRequest;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.SupplierResponse;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.UpdateSupplierRequest;

public interface SupplierService {
    SupplierResponse create(CreateSupplierRequest request);
    SupplierResponse update(Long id, UpdateSupplierRequest request);
    void delete(Long id);
    SupplierResponse getById(Long id);
    ResultPaginationDTO getAll(Pageable pageable);
}
