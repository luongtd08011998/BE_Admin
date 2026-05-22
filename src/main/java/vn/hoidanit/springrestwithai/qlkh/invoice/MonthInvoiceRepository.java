package vn.hoidanit.springrestwithai.qlkh.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.MonthInvoiceReadingItemResponse;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoice;

import java.util.List;
import java.util.Optional;

import vn.hoidanit.springrestwithai.qlkh.invoice.dto.ConsumptionHistoryItemResponse;

@Repository
public interface MonthInvoiceRepository extends JpaRepository<MonthInvoice, Integer> {

    @Query("""
            SELECT NEW vn.hoidanit.springrestwithai.qlkh.invoice.dto.ConsumptionHistoryItemResponse(
                m.yearMonth, m.oldVal, m.newVal,
                CASE WHEN m.newVal IS NOT NULL AND m.oldVal IS NOT NULL
                     THEN (m.newVal - m.oldVal) ELSE NULL END)
            FROM MonthInvoice m
            JOIN Customer c ON m.customerId = c.customerId
            WHERE c.digiCode = :digiCode
            AND m.yearMonth >= :fromYm AND m.yearMonth <= :toYm
            ORDER BY m.yearMonth ASC
            """)
    List<ConsumptionHistoryItemResponse> findConsumptionHistory(
            @Param("digiCode") String digiCode,
            @Param("fromYm") String fromYearMonth,
            @Param("toYm") String toYearMonth);

    Optional<MonthInvoice> findByCustomerIdAndFkey(Integer customerId, String fkey);

    Optional<MonthInvoice> findByCustomerIdAndRootKey(Integer customerId, String rootKey);

    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.customerId = :customerId
            AND (m.fkey IS NOT NULL AND m.fkey <> '')
            """)
    Page<MonthInvoice> findByCustomerId(
            @Param("customerId") Integer customerId, Pageable pageable);

    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.customerId = :customerId
            AND (m.fkey IS NOT NULL AND m.fkey <> '')
            AND LOWER(m.yearMonth) LIKE LOWER(CONCAT('%', :ym, '%'))
            """)
    Page<MonthInvoice> findByCustomerIdAndYearMonthContaining(
            @Param("customerId") Integer customerId,
            @Param("ym") String yearMonth,
            Pageable pageable);

    @Query("""
            SELECT NEW vn.hoidanit.springrestwithai.qlkh.invoice.dto.MonthInvoiceReadingItemResponse(
                c.digiCode, m.oldVal, m.newVal)
            FROM MonthInvoice m, Customer c
            WHERE m.customerId = c.customerId
            AND m.yearMonth = :ym
            ORDER BY m.yearMonth ASC, c.customerId ASC, m.monthInvoiceId ASC
            """)
    List<MonthInvoiceReadingItemResponse> findReadingsByYearMonth(@Param("ym") String yearMonth);

    @Query("""
            SELECT NEW vn.hoidanit.springrestwithai.qlkh.invoice.dto.MonthInvoiceReadingItemResponse(
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
    /**
     * Lấy danh sách hóa đơn MỚI kèm thông tin KH — dành cho InvoiceNotificationScheduler.
     * JOIN Customer để lấy tên + digiCode + amount trong 1 câu query, tránh N+1.
     */
    @Query("""
            SELECT new vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO(
                m.customerId, m.monthInvoiceId, m.yearMonth, c.digiCode, c.name,
                (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)),
                c.address,
                (SELECT CASE WHEN COUNT(m2) > 0 THEN true ELSE false END
                 FROM MonthInvoice m2
                 WHERE m2.customerId = m.customerId
                 AND m2.yearMonth = m.yearMonth
                 AND (COALESCE(m2.amount, 0) + COALESCE(m2.envFee, 0) + COALESCE(m2.taxFee, 0)) = 0)
            )
            FROM MonthInvoice m
            JOIN Customer c ON m.customerId = c.customerId
            WHERE m.createdDate LIKE CONCAT(:datePrefix, '%')
            """)
    Page<vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO> findInvoiceInfoByCreatedDatePrefix(
            @Param("datePrefix") String datePrefix, Pageable pageable);

    /**
     * Lấy hóa đơn đã thanh toán kèm thông tin KH — dành cho PaymentNotificationScheduler.
     * LOẠI TRỪ những invoice đã được thông báo.
     */
    @Query("""
            SELECT new vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO(
                m.customerId, m.monthInvoiceId, m.yearMonth, c.digiCode, c.name,
                (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)),
                c.address,
                (SELECT CASE WHEN COUNT(m2) > 0 THEN true ELSE false END
                 FROM MonthInvoice m2
                 WHERE m2.customerId = m.customerId
                 AND m2.yearMonth = m.yearMonth
                 AND (COALESCE(m2.amount, 0) + COALESCE(m2.envFee, 0) + COALESCE(m2.taxFee, 0)) = 0)
            )
            FROM MonthInvoice m
            JOIN Customer c ON m.customerId = c.customerId
            WHERE m.paymentStatus = 2
            AND m.yearMonth >= :fromYearMonth
            AND m.monthInvoiceId NOT IN :excludeIds
            """)
    Page<vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO> findPaidInvoiceInfoExcluding(
            @Param("fromYearMonth") String fromYearMonth,
            @Param("excludeIds") List<Integer> excludeIds,
            Pageable pageable);

    /**
     * Lấy hóa đơn có trạng thái ĐÃ THANH TOÁN (paymentStatus = 2) trong khoảng từ {@code fromYearMonth} trở về sau.
     * {@code fromYearMonth} định dạng "YYYYMM", ví dụ "202602" — dùng để giới hạn 3 tháng gần nhất.
     */
    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.paymentStatus = 2
            AND m.yearMonth >= :fromYearMonth
            """)
    Page<MonthInvoice> findRecentPaidInvoices(@Param("fromYearMonth") String fromYearMonth, Pageable pageable);

    /**
     * Lấy hóa đơn đã thanh toán nhưng LOẠI TRỪ những invoice đã được thông báo (Entity version - giữ lại để tương thích).
     */
    @Query("""
            SELECT m FROM MonthInvoice m
            WHERE m.paymentStatus = 2
            AND m.yearMonth >= :fromYearMonth
            AND m.monthInvoiceId NOT IN :excludeIds
            """)
    Page<MonthInvoice> findRecentPaidInvoicesExcluding(
            @Param("fromYearMonth") String fromYearMonth,
            @Param("excludeIds") List<Integer> excludeIds,
            Pageable pageable);

    @Query("SELECT m FROM MonthInvoice m WHERE m.customerId = :customerId AND m.yearMonth = :yearMonth")
    List<MonthInvoice> findByCustomerIdAndYearMonth(@Param("customerId") Integer customerId, @Param("yearMonth") String yearMonth);

    @Query("SELECT m FROM MonthInvoice m WHERE m.customerId = :customerId AND m.yearMonth = :yearMonth AND m.paymentStatus = 2")
    List<MonthInvoice> findPaidByCustomerIdAndYearMonth(@Param("customerId") Integer customerId, @Param("yearMonth") String yearMonth);

    @Query("""
            SELECT NEW vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.AdminInvoiceResponse(
                m.monthInvoiceId,
                c.digiCode, c.name, 
                (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)), 
                m.yearMonth, 
                m.fkey,
                m.paymentStatus,
                (SELECT CASE WHEN COUNT(m2) > 0 THEN true ELSE false END 
                 FROM MonthInvoice m2 
                 WHERE m2.customerId = m.customerId 
                 AND m2.yearMonth = m.yearMonth 
                 AND (COALESCE(m2.amount, 0) + COALESCE(m2.envFee, 0) + COALESCE(m2.taxFee, 0)) = 0),
                m.blankNo,
                m.roadId) 
            FROM MonthInvoice m, Customer c 
            WHERE m.customerId = c.customerId
            AND (m.fkey IS NOT NULL AND m.fkey <> '')
            AND (:yearMonth IS NULL OR :yearMonth = '' OR m.yearMonth = :yearMonth)
            AND (:roadId IS NULL OR m.roadId = :roadId)
            AND (:paymentStatus IS NULL OR m.paymentStatus = :paymentStatus)
            AND (:customerName IS NULL OR :customerName = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :customerName, '%')))
            AND (:digiCode IS NULL OR :digiCode = '' OR LOWER(c.digiCode) LIKE LOWER(CONCAT('%', :digiCode, '%')))
            AND (:remindStatus IS NULL
                 OR (:remindStatus = 1 AND m.monthInvoiceId IN :remindedIds)
                 OR (:remindStatus = 0 AND m.monthInvoiceId NOT IN :remindedIds)
                 OR (:remindStatus = 2 AND m.monthInvoiceId IN :overdueIds)
                 OR (:remindStatus = 3 AND m.monthInvoiceId IN :cutwaterIds))
            ORDER BY m.yearMonth DESC, m.monthInvoiceId DESC
            """)
    Page<vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.AdminInvoiceResponse> findAdminInvoices(
            @Param("yearMonth") String yearMonth,
            @Param("paymentStatus") Integer paymentStatus,
            @Param("customerName") String customerName,
            @Param("digiCode") String digiCode,
            @Param("remindStatus") Integer remindStatus,
            @Param("roadId") Integer roadId,
            @Param("remindedIds") java.util.List<Integer> remindedIds,
            @Param("overdueIds") java.util.List<Integer> overdueIds,
            @Param("cutwaterIds") java.util.List<Integer> cutwaterIds,
            Pageable pageable);

    @Query("""
            SELECT new vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO(
                m.customerId, m.monthInvoiceId, m.yearMonth, c.digiCode, c.name,
                (COALESCE(m.amount, 0) + COALESCE(m.envFee, 0) + COALESCE(m.taxFee, 0)),
                c.address,
                (SELECT CASE WHEN COUNT(m2) > 0 THEN true ELSE false END
                 FROM MonthInvoice m2
                 WHERE m2.customerId = m.customerId
                 AND m2.yearMonth = m.yearMonth
                 AND (COALESCE(m2.amount, 0) + COALESCE(m2.envFee, 0) + COALESCE(m2.taxFee, 0)) = 0)
            )
            FROM MonthInvoice m
            JOIN Customer c ON m.customerId = c.customerId
            WHERE m.yearMonth = :yearMonth
            AND m.paymentStatus = 1
            """)
    java.util.List<vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO> findUnpaidInvoiceDTOsByYearMonth(@Param("yearMonth") String yearMonth);
}
