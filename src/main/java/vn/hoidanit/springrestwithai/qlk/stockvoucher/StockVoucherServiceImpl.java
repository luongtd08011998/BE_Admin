package vn.hoidanit.springrestwithai.qlk.stockvoucher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.BadRequestException;
import vn.hoidanit.springrestwithai.exception.DuplicateResourceException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.qlk.inventory.InventoryService;
import vn.hoidanit.springrestwithai.qlk.material.Material;
import vn.hoidanit.springrestwithai.qlk.material.MaterialRepository;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.CreateStockVoucherRequest;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.CreateVoucherDetailRequest;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.StockVoucherResponse;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.UpdateStockVoucherRequest;
import vn.hoidanit.springrestwithai.qlk.supplier.Supplier;
import vn.hoidanit.springrestwithai.qlk.supplier.SupplierRepository;
import vn.hoidanit.springrestwithai.qlk.warehouse.Warehouse;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseRepository;
import java.math.BigDecimal;

@Service
@Transactional
public class StockVoucherServiceImpl implements StockVoucherService {

    private final StockVoucherRepository stockVoucherRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final InventoryService inventoryService;

    public StockVoucherServiceImpl(StockVoucherRepository stockVoucherRepository,
                                   WarehouseRepository warehouseRepository,
                                   SupplierRepository supplierRepository,
                                   UserRepository userRepository,
                                   MaterialRepository materialRepository,
                                   InventoryService inventoryService) {
        this.stockVoucherRepository = stockVoucherRepository;
        this.warehouseRepository = warehouseRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.materialRepository = materialRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public StockVoucherResponse create(CreateStockVoucherRequest request, Long userId, Long warehouseId) {
        if (stockVoucherRepository.existsByVoucherCode(request.voucherCode())) {
            throw new DuplicateResourceException("Phiếu kho", "voucherCode", request.voucherCode());
        }

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Kho", "id", warehouseId.toString()));

        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId.toString()));

        Supplier supplier = null;
        if (request.supplierId() != null) {
            supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nhà cung cấp", "id", request.supplierId().toString()));
        }

        StockVoucher voucher = StockVoucher.builder()
                .voucherCode(request.voucherCode())
                .type(request.type())
                .status(VoucherStatus.NHAP_BAN)
                .issuedDate(request.issuedDate())
                .note(request.note())
                .warehouse(warehouse)
                .supplier(supplier)
                .createdBy(createdBy)
                .build();

        if (request.details() != null) {
            for (CreateVoucherDetailRequest dReq : request.details()) {
                Material material = materialRepository.findById(dReq.materialId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vật tư", "id", dReq.materialId().toString()));
                
                BigDecimal amount = dReq.unitPrice() != null ? dReq.unitPrice().multiply(BigDecimal.valueOf(dReq.quantity())) : null;
                
                VoucherDetail detail = VoucherDetail.builder()
                        .material(material)
                        .quantity(dReq.quantity())
                        .unitPrice(dReq.unitPrice())
                        .amount(amount)
                        .note(dReq.note())
                        .build();
                voucher.addDetail(detail);
            }
        }

        return StockVoucherResponse.from(stockVoucherRepository.save(voucher));
    }

    @Override
    public StockVoucherResponse update(Long id, UpdateStockVoucherRequest request) {
        StockVoucher voucher = getStockVoucherById(id);
        
        if (voucher.getStatus() != VoucherStatus.NHAP_BAN) {
            throw new BadRequestException("Chỉ được cập nhật phiếu ở trạng thái NHAP_BAN");
        }

        Supplier supplier = null;
        if (request.supplierId() != null) {
            supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nhà cung cấp", "id", request.supplierId().toString()));
        }

        voucher.setIssuedDate(request.issuedDate());
        voucher.setNote(request.note());
        voucher.setSupplier(supplier);

        // Update details (simplified: remove all and add new)
        voucher.getDetails().clear();
        
        if (request.details() != null) {
            for (CreateVoucherDetailRequest dReq : request.details()) {
                Material material = materialRepository.findById(dReq.materialId())
                        .orElseThrow(() -> new ResourceNotFoundException("Vật tư", "id", dReq.materialId().toString()));
                
                BigDecimal amount = dReq.unitPrice() != null ? dReq.unitPrice().multiply(BigDecimal.valueOf(dReq.quantity())) : null;
                
                VoucherDetail detail = VoucherDetail.builder()
                        .material(material)
                        .quantity(dReq.quantity())
                        .unitPrice(dReq.unitPrice())
                        .amount(amount)
                        .note(dReq.note())
                        .build();
                voucher.addDetail(detail);
            }
        }

        return StockVoucherResponse.from(stockVoucherRepository.save(voucher));
    }

    @Override
    public void delete(Long id) {
        StockVoucher voucher = getStockVoucherById(id);
        if (voucher.getStatus() != VoucherStatus.NHAP_BAN) {
            throw new BadRequestException("Chỉ được xóa phiếu ở trạng thái NHAP_BAN");
        }
        stockVoucherRepository.delete(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public StockVoucherResponse getById(Long id) {
        return StockVoucherResponse.from(getStockVoucherById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(Pageable pageable, Long warehouseId, String type, String status) {
        Specification<StockVoucher> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
            
            if (StringUtils.hasText(type)) {
                try {
                    VoucherType vType = VoucherType.valueOf(type.toUpperCase());
                    predicates.add(cb.equal(root.get("type"), vType));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }
            
            if (StringUtils.hasText(status)) {
                try {
                    VoucherStatus vStatus = VoucherStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), vStatus));
                } catch (IllegalArgumentException e) {
                    // Ignore
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StockVoucher> page = stockVoucherRepository.findAll(spec, pageable);
        return ResultPaginationDTO.fromPage(page.map(StockVoucherResponse::from));
    }

    @Override
    public StockVoucherResponse submit(Long id) {
        StockVoucher voucher = getStockVoucherById(id);
        
        if (voucher.getStatus() != VoucherStatus.NHAP_BAN) {
            throw new BadRequestException("Chỉ được trình duyệt phiếu ở trạng thái NHAP_BAN");
        }
        
        if (voucher.getDetails().isEmpty()) {
            throw new BadRequestException("Không thể trình duyệt phiếu không có chi tiết vật tư");
        }
        
        voucher.setStatus(VoucherStatus.CHO_DUYET);
        
        // If it is OUTBOUND, reserve the stock
        if (voucher.getType() == VoucherType.XUAT_KHO) {
            inventoryService.reserveOutbound(voucher);
        }
        
        return StockVoucherResponse.from(stockVoucherRepository.save(voucher));
    }

    @Override
    public StockVoucherResponse approve(Long id, Long approverId) {
        StockVoucher voucher = getStockVoucherById(id);
        
        if (voucher.getStatus() != VoucherStatus.CHO_DUYET) {
            throw new BadRequestException("Chỉ được duyệt phiếu ở trạng thái CHO_DUYET");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", approverId.toString()));
                
        voucher.setStatus(VoucherStatus.DA_DUYET);
        voucher.setApprovedBy(approver);
        voucher.setApprovedAt(Instant.now());
        
        // Update stock
        if (voucher.getType() == VoucherType.NHAP_KHO) {
            inventoryService.processInbound(voucher);
        } else if (voucher.getType() == VoucherType.XUAT_KHO) {
            inventoryService.processOutbound(voucher);
        }
        
        return StockVoucherResponse.from(stockVoucherRepository.save(voucher));
    }

    @Override
    public StockVoucherResponse reject(Long id, String reason) {
        StockVoucher voucher = getStockVoucherById(id);
        
        if (voucher.getStatus() != VoucherStatus.CHO_DUYET) {
            throw new BadRequestException("Chỉ được từ chối phiếu ở trạng thái CHO_DUYET");
        }
        
        voucher.setStatus(VoucherStatus.TU_CHOI);
        voucher.setRejectReason(reason);
        
        // Cancel reservation if it was an outbound voucher
        if (voucher.getType() == VoucherType.XUAT_KHO) {
            inventoryService.cancelOutboundReservation(voucher);
        }
        
        return StockVoucherResponse.from(stockVoucherRepository.save(voucher));
    }

    private StockVoucher getStockVoucherById(Long id) {
        return stockVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phiếu kho", "id", id.toString()));
    }
}
