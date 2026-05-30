package vn.hoidanit.springrestwithai.qlk.stockvoucher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherDetailRepository extends JpaRepository<VoucherDetail, Long> {
    List<VoucherDetail> findByStockVoucherId(Long voucherId);
}
