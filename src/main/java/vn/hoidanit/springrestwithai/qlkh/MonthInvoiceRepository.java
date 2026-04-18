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
}
