package vn.hoidanit.springrestwithai.qlkh.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.payment.dto.PaymentLineResponse;
import vn.hoidanit.springrestwithai.qlkh.payment.entity.PaymentLine;

@Service
public class PaymentLineServiceImpl implements PaymentLineService {

    private final PaymentLineRepository paymentLineRepository;

    public PaymentLineServiceImpl(PaymentLineRepository paymentLineRepository) {
        this.paymentLineRepository = paymentLineRepository;
    }

    @Override
    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public ResultPaginationDTO getPaymentHistory(Customer customer, Pageable pageable) {
        Page<PaymentLine> source = paymentLineRepository.findByCustomerIdOrderByPaymentLineIdDesc(
                customer.getCustomerId(), pageable);
        Page<PaymentLineResponse> page = source.map(PaymentLineResponse::fromEntity);
        return ResultPaginationDTO.fromPage(page);
    }

    @Override
    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public ResultPaginationDTO getAdminPaymentLines(Integer customerId, String yearMonth, Pageable pageable) {
        Page<PaymentLine> source;
        String ym = (yearMonth != null) ? yearMonth.trim() : "";
        if (customerId != null && !ym.isEmpty()) {
            source = paymentLineRepository.findByCustomerIdAndYearMonth(customerId, ym, pageable);
        } else if (customerId != null) {
            source = paymentLineRepository.findByCustomerId(customerId, pageable);
        } else if (!ym.isEmpty()) {
            source = paymentLineRepository.findByYearMonth(ym, pageable);
        } else {
            source = paymentLineRepository.findAll(pageable);
        }
        Page<PaymentLineResponse> page = source.map(PaymentLineResponse::fromEntity);
        return ResultPaginationDTO.fromPage(page);
    }
}
