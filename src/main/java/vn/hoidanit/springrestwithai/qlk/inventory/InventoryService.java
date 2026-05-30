package vn.hoidanit.springrestwithai.qlk.inventory;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.StockVoucher;

public interface InventoryService {
    ResultPaginationDTO getStocksByWarehouse(Long warehouseId, String search, Pageable pageable);
    ResultPaginationDTO getTransactionsByWarehouse(Long warehouseId, String search, String type, String date, Pageable pageable);
    ResultPaginationDTO getSnapshotsByWarehouse(Long warehouseId, Pageable pageable);
    
    // Core inventory operations called by StockVoucherService
    void processInbound(StockVoucher voucher);
    void reserveOutbound(StockVoucher voucher);
    void processOutbound(StockVoucher voucher);
    void cancelOutboundReservation(StockVoucher voucher);
    
    // Snapshot generation
    void generateSnapshot(Long warehouseId, String period);
}
