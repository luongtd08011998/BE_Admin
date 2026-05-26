package vn.hoidanit.springrestwithai.qlkh.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.qlkh.payment.entity.PaymentLine;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentLineRepository extends JpaRepository<PaymentLine, Integer> {

    /**
     * Lấy ID lớn nhất trong bảng paymentline phục vụ việc lập mốc đồng bộ hóa (bootstrap).
     */
    @Query("SELECT MAX(p.paymentLineId) FROM PaymentLine p")
    Optional<Integer> findMaxPaymentLineId();

    /**
     * Tìm tất cả các dòng thanh toán mới phát sinh có ID lớn hơn mốc đã lưu, sắp xếp tăng dần theo ID.
     */
    List<PaymentLine> findByPaymentLineIdGreaterThanOrderByPaymentLineIdAsc(Integer lastId);

    org.springframework.data.domain.Page<PaymentLine> findByCustomerIdOrderByPaymentLineIdDesc(Integer customerId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<PaymentLine> findByCustomerId(Integer customerId, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<PaymentLine> findByYearMonth(String yearMonth, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<PaymentLine> findByCustomerIdAndYearMonth(Integer customerId, String yearMonth, org.springframework.data.domain.Pageable pageable);
}

