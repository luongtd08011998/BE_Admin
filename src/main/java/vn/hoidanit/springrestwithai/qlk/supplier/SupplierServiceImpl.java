package vn.hoidanit.springrestwithai.qlk.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.CreateSupplierRequest;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.SupplierResponse;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.UpdateSupplierRequest;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public SupplierResponse create(CreateSupplierRequest request) {
        if (supplierRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Nhà cung cấp", "name", request.name());
        }

        Supplier supplier = Supplier.builder()
                .name(request.name())
                .phone(request.phone())
                .address(request.address())
                .email(request.email())
                .status(SupplierStatus.DANG_HOP_TAC)
                .build();

        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponse update(Long id, UpdateSupplierRequest request) {
        Supplier supplier = getSupplierById(id);

        if (!supplier.getName().equals(request.name()) && supplierRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Nhà cung cấp", "name", request.name());
        }

        supplier.setName(request.name());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        supplier.setEmail(request.email());
        if (request.status() != null) {
            supplier.setStatus(request.status());
        }

        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    @Override
    public void delete(Long id) {
        Supplier supplier = getSupplierById(id);
        supplierRepository.delete(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id) {
        return SupplierResponse.from(getSupplierById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(Pageable pageable) {
        Page<Supplier> page = supplierRepository.findAll(pageable);
        return ResultPaginationDTO.fromPage(page.map(SupplierResponse::from));
    }

    private Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhà cung cấp", "id", id.toString()));
    }
}
