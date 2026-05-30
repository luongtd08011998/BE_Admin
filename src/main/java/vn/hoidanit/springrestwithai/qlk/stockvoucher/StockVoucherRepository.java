package vn.hoidanit.springrestwithai.qlk.stockvoucher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockVoucherRepository extends JpaRepository<StockVoucher, Long>, JpaSpecificationExecutor<StockVoucher> {
    Optional<StockVoucher> findByVoucherCode(String voucherCode);
    boolean existsByVoucherCode(String voucherCode);
    Page<StockVoucher> findByWarehouseId(Long warehouseId, Pageable pageable);
    long countByWarehouseId(Long warehouseId);
}
