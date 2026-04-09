package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.qlkh.entity.MonthInvoice;

@Repository
public interface MonthInvoiceRepository extends JpaRepository<MonthInvoice, Integer> {

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
}
