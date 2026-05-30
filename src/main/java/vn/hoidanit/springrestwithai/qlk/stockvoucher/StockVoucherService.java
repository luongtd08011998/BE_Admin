package vn.hoidanit.springrestwithai.qlk.stockvoucher;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.CreateStockVoucherRequest;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.StockVoucherResponse;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.dto.UpdateStockVoucherRequest;

public interface StockVoucherService {
    StockVoucherResponse create(CreateStockVoucherRequest request, Long userId, Long warehouseId);
    StockVoucherResponse update(Long id, UpdateStockVoucherRequest request);
    void delete(Long id);
    StockVoucherResponse getById(Long id);
    ResultPaginationDTO getAll(Pageable pageable, Long warehouseId, String type, String status);
    StockVoucherResponse submit(Long id);
    StockVoucherResponse approve(Long id, Long approverId);
    StockVoucherResponse reject(Long id, String reason);
}
