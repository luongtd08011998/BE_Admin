package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.qlkh.dto.MonthInvoiceReadingItemResponse;
import vn.hoidanit.springrestwithai.qlkh.entity.MonthInvoice;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthInvoiceRepository extends JpaRepository<MonthInvoice, Integer> {

    Optional<MonthInvoice> findByCustomerIdAndFkey(Integer customerId, String fkey);

    Optional<MonthInvoice> findByCustomerIdAndRootKey(Integer customerId, String rootKey);

    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.customerId = :customerId
            AND (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)) <> 0
            """)
    Page<MonthInvoice> findByCustomerIdExcludingZeroTotal(
            @Param("customerId") Integer customerId, Pageable pageable);

    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.customerId = :customerId
            AND LOWER(m.yearMonth) LIKE LOWER(CONCAT('%', :ym, '%'))
            AND (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)) <> 0
            """)
    Page<MonthInvoice> findByCustomerIdAndYearMonthContainingExcludingZeroTotal(
            @Param("customerId") Integer customerId,
            @Param("ym") String yearMonth,
            Pageable pageable);

    @Query("""
            SELECT NEW vn.hoidanit.springrestwithai.qlkh.dto.MonthInvoiceReadingItemResponse(
                c.digiCode, m.oldVal, m.newVal)
            FROM MonthInvoice m, Customer c
            WHERE m.customerId = c.customerId
            AND m.yearMonth = :ym
            ORDER BY m.yearMonth ASC, c.customerId ASC, m.monthInvoiceId ASC
            """)
    List<MonthInvoiceReadingItemResponse> findReadingsByYearMonth(@Param("ym") String yearMonth);

    @Query("""
            SELECT NEW vn.hoidanit.springrestwithai.qlkh.dto.MonthInvoiceReadingItemResponse(
                c.digiCode, m.oldVal, m.newVal)
            FROM MonthInvoice m, Customer c
            WHERE m.customerId = c.customerId
            AND m.yearMonth >= :fromYm AND m.yearMonth <= :toYm
            ORDER BY m.yearMonth ASC, c.customerId ASC, m.monthInvoiceId ASC
            """)
    List<MonthInvoiceReadingItemResponse> findReadingsByYearMonthRange(
            @Param("fromYm") String fromYearMonth,
            @Param("toYm") String toYearMonth);

    /**
     * Lấy danh sách hóa đơn có tổng tiền > 0 được tạo vào ngày {@code datePrefix}.
     * {@code datePrefix} thường là "yyyy-MM-dd" hoặc "yyyyMMdd" tùy format lưu trong DB.
     */
    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.createdDate LIKE CONCAT(:datePrefix, '%')
            AND (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)) <> 0
            """)
    Page<MonthInvoice> findByCreatedDatePrefix(@Param("datePrefix") String datePrefix, Pageable pageable);

    /**
     * Lấy hóa đơn có trạng thái ĐÃ THANH TOÁN (paymentStatus = 2) trong khoảng từ {@code fromYearMonth} trở về sau.
     * {@code fromYearMonth} định dạng "YYYYMM", ví dụ "202602" — dùng để giới hạn 3 tháng gần nhất.
     */
    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.paymentStatus = 2
            AND m.yearMonth >= :fromYearMonth
            AND (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)) <> 0
            """)
    Page<MonthInvoice> findRecentPaidInvoices(@Param("fromYearMonth") String fromYearMonth, Pageable pageable);

    /**
     * Lấy hóa đơn đã thanh toán nhưng LOẠI TRỪ những invoice đã được thông báo.
     * {@code excludeIds} là danh sách monthInvoiceId lấy từ bảng notified_payment (DB primary).
     * Giảm số record load mỗi lần cron chạy từ ~24.000 xuống chỉ còn vài chục.
     */
    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.paymentStatus = 2
            AND m.yearMonth >= :fromYearMonth
            AND (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)) <> 0
            AND m.monthInvoiceId NOT IN :excludeIds
            """)
    Page<MonthInvoice> findRecentPaidInvoicesExcluding(
            @Param("fromYearMonth") String fromYearMonth,
            @Param("excludeIds") List<Integer> excludeIds,
            Pageable pageable);
}
