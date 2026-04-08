package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hoidanit.springrestwithai.qlkh.entity.MonthInvoice;

@Repository
public interface MonthInvoiceRepository extends JpaRepository<MonthInvoice, Integer> {

    Page<MonthInvoice> findByCustomerId(Integer customerId, Pageable pageable);
}
