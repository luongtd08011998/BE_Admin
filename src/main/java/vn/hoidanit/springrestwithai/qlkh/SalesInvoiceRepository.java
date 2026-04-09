package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.hoidanit.springrestwithai.qlkh.entity.SalesInvoice;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Integer> {

    Page<SalesInvoice> findByCustomerId(Integer customerId, Pageable pageable);

    @Query("""
            SELECT s FROM SalesInvoice s
            WHERE s.customerId = :customerId
            AND LOWER(s.templateCode) LIKE LOWER(CONCAT('%', :tc, '%'))
            """)
    Page<SalesInvoice> findByCustomerIdAndTemplateCodeContaining(
            @Param("customerId") Integer customerId,
            @Param("tc") String templateCode,
            Pageable pageable);
}
