package vn.hoidanit.springrestwithai.qlkh.payment;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;

public interface PaymentLineService {

    /**
     * Lấy lịch sử thanh toán của khách hàng hiện tại (phân trang).
     */
    ResultPaginationDTO getPaymentHistory(Customer customer, Pageable pageable);

    /**
     * Tra cứu danh sách dòng thanh toán cho Admin (phân trang, lọc theo customerId, yearMonth).
     */
    ResultPaginationDTO getAdminPaymentLines(Integer customerId, String yearMonth, Pageable pageable);
}
