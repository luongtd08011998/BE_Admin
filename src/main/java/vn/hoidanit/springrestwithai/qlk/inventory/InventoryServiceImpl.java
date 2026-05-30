package vn.hoidanit.springrestwithai.qlk.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.BadRequestException;
import vn.hoidanit.springrestwithai.qlk.inventory.dto.InventorySnapshotResponse;
import vn.hoidanit.springrestwithai.qlk.inventory.dto.InventoryStockResponse;
import vn.hoidanit.springrestwithai.qlk.inventory.dto.InventoryTransactionResponse;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.StockVoucher;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.VoucherDetail;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryStockRepository stockRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventorySnapshotRepository snapshotRepository;

    public InventoryServiceImpl(InventoryStockRepository stockRepository,
                                InventoryTransactionRepository transactionRepository,
                                InventorySnapshotRepository snapshotRepository) {
        this.stockRepository = stockRepository;
        this.transactionRepository = transactionRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getStocksByWarehouse(Long warehouseId, String search, Pageable pageable) {
        Page<InventoryStock> page = stockRepository.findByWarehouseIdAndSearch(warehouseId, search, pageable);
        return ResultPaginationDTO.fromPage(page.map(InventoryStockResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getTransactionsByWarehouse(Long warehouseId, String search, String type, String date, Pageable pageable) {
        Specification<InventoryTransaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
            
            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.trim().toLowerCase() + "%";
                Predicate codeMatch = cb.like(cb.lower(root.get("material").get("code")), searchLike);
                Predicate nameMatch = cb.like(cb.lower(root.get("material").get("name")), searchLike);
                predicates.add(cb.or(codeMatch, nameMatch));
            }
            
            if (StringUtils.hasText(type)) {
                try {
                    TransactionType txType = TransactionType.valueOf(type.toUpperCase());
                    predicates.add(cb.equal(root.get("transactionType"), txType));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid type
                }
            }
            
            if (StringUtils.hasText(date)) {
                try {
                    LocalDate localDate = LocalDate.parse(date);
                    Instant startOfDay = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                    Instant endOfDay = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startOfDay));
                    predicates.add(cb.lessThan(root.get("createdAt"), endOfDay));
                } catch (Exception e) {
                    // Ignore invalid date
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<InventoryTransaction> page = transactionRepository.findAll(spec, pageable);
        return ResultPaginationDTO.fromPage(page.map(InventoryTransactionResponse::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getSnapshotsByWarehouse(Long warehouseId, Pageable pageable) {
        Page<InventorySnapshot> page = snapshotRepository.findByWarehouseId(warehouseId, pageable);
        return ResultPaginationDTO.fromPage(page.map(InventorySnapshotResponse::from));
    }

    @Override
    public void processInbound(StockVoucher voucher) {
        for (VoucherDetail detail : voucher.getDetails()) {
            InventoryStock stock = getOrCreateStock(voucher.getWarehouse().getId(), detail.getMaterial().getId());
            
            int before = stock.getQuantity();
            int change = detail.getQuantity();
            stock.setQuantity(before + change);
            
            stockRepository.save(stock);
            
            recordTransaction(voucher, detail, TransactionType.NHAP, change, before, stock.getQuantity());
        }
    }

    @Override
    public void reserveOutbound(StockVoucher voucher) {
        for (VoucherDetail detail : voucher.getDetails()) {
            InventoryStock stock = getOrCreateStock(voucher.getWarehouse().getId(), detail.getMaterial().getId());
            
            int available = stock.getQuantity() - stock.getReservedQuantity();
            if (available < detail.getQuantity()) {
                throw new BadRequestException("Tồn kho không đủ cho vật tư: " + detail.getMaterial().getCode() 
                    + ". Tồn thực: " + stock.getQuantity() + ", Đã giữ: " + stock.getReservedQuantity() 
                    + ", Yêu cầu: " + detail.getQuantity());
            }
            
            stock.setReservedQuantity(stock.getReservedQuantity() + detail.getQuantity());
            stockRepository.save(stock);
            
            // We can optionally record a transaction for reservation
            recordTransaction(voucher, detail, TransactionType.DAT_TRUOC, detail.getQuantity(), stock.getQuantity(), stock.getQuantity());
        }
    }

    @Override
    public void processOutbound(StockVoucher voucher) {
        for (VoucherDetail detail : voucher.getDetails()) {
            InventoryStock stock = getOrCreateStock(voucher.getWarehouse().getId(), detail.getMaterial().getId());
            
            int before = stock.getQuantity();
            int change = detail.getQuantity();
            
            // Deduct actual stock and reserved stock
            stock.setQuantity(before - change);
            stock.setReservedQuantity(stock.getReservedQuantity() - change);
            
            stockRepository.save(stock);
            
            recordTransaction(voucher, detail, TransactionType.XUAT, -change, before, stock.getQuantity());
        }
    }

    @Override
    public void cancelOutboundReservation(StockVoucher voucher) {
        for (VoucherDetail detail : voucher.getDetails()) {
            InventoryStock stock = getOrCreateStock(voucher.getWarehouse().getId(), detail.getMaterial().getId());
            
            stock.setReservedQuantity(stock.getReservedQuantity() - detail.getQuantity());
            stockRepository.save(stock);
            
            recordTransaction(voucher, detail, TransactionType.HUY_DAT_TRUOC, -detail.getQuantity(), stock.getQuantity(), stock.getQuantity());
        }
    }

    @Override
    public void generateSnapshot(Long warehouseId, String period) {
        // Implementation for monthly snapshot generation
        // Can be scheduled at the end of each month
    }
    
    private InventoryStock getOrCreateStock(Long warehouseId, Long materialId) {
        return stockRepository.findByWarehouseIdAndMaterialId(warehouseId, materialId)
                .orElseGet(() -> {
                    InventoryStock newStock = InventoryStock.builder()
                        .warehouse(vn.hoidanit.springrestwithai.qlk.warehouse.Warehouse.builder().id(warehouseId).build())
                        .material(vn.hoidanit.springrestwithai.qlk.material.Material.builder().id(materialId).build())
                        .quantity(0)
                        .reservedQuantity(0)
                        .build();
                    return stockRepository.save(newStock);
                });
    }
    
    private void recordTransaction(StockVoucher voucher, VoucherDetail detail, TransactionType type, 
                                   int change, int before, int after) {
        InventoryTransaction tx = InventoryTransaction.builder()
                .warehouse(voucher.getWarehouse())
                .material(detail.getMaterial())
                .transactionType(type)
                .quantityChange(change)
                .quantityBefore(before)
                .quantityAfter(after)
                .stockVoucher(voucher)
                .note(detail.getNote())
                .createdBy(voucher.getCreatedBy())
                .build();
                
        transactionRepository.save(tx);
    }
}
