package vn.hoidanit.springrestwithai.qlkh.paybill;

import org.springframework.stereotype.Service;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoice;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.paybill.dto.PaybillBillItem;
import vn.hoidanit.springrestwithai.qlkh.paybill.dto.PaybillQrRequest;
import vn.hoidanit.springrestwithai.qlkh.paybill.dto.PaybillQrResponse;
import vn.hoidanit.springrestwithai.qlkh.paybill.dto.PaybillQrResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaybillQrService {

    private final CustomerRepository customerRepository;
    private final MonthInvoiceRepository monthInvoiceRepository;
    private final PaybillQrClient paybillQrClient;
    private final PaybillProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public PaybillQrService(CustomerRepository customerRepository,
                            MonthInvoiceRepository monthInvoiceRepository,
                            PaybillQrClient paybillQrClient,
                            PaybillProperties props) {
        this.customerRepository = customerRepository;
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.paybillQrClient = paybillQrClient;
        this.props = props;
    }

    public PaybillQrResult createQrCode(Integer customerId, List<Integer> monthInvoiceIds) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        List<MonthInvoice> invoices = monthInvoiceRepository.findAllById(monthInvoiceIds);
        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn nào.");
        }

        List<PaybillBillItem> bills = new ArrayList<>();
        long totalAmount = 0;
        List<String> periods = new ArrayList<>();

        for (MonthInvoice inv : invoices) {
            if (!inv.getCustomerId().equals(customerId)) {
                throw new IllegalArgumentException("Hóa đơn " + inv.getMonthInvoiceId() + " không thuộc về khách hàng này.");
            }
            if (inv.getPaymentStatus() != null && inv.getPaymentStatus() == 2) {
                throw new IllegalArgumentException("Hóa đơn " + inv.getMonthInvoiceId() + " đã được thanh toán.");
            }

            long money = Math.round((inv.getAmount() != null ? inv.getAmount() : 0) + 
                                    (inv.getEnvFee() != null ? inv.getEnvFee() : 0) + 
                                    (inv.getTaxFee() != null ? inv.getTaxFee() : 0));
            
            String period = formatPeriod(inv.getYearMonth());
            
            PaybillBillItem item = new PaybillBillItem(String.valueOf(inv.getMonthInvoiceId()), period, money);
            bills.add(item);
            totalAmount += money;
            periods.add(period);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyyHHmmss"));

        PaybillQrRequest request = new PaybillQrRequest();
        request.setBillerCode(props.getBillerCode());
        request.setServiceCode(props.getServiceCode());
        request.setTimestamp(timestamp);
        // Ensure customerCode does not exceed 20 characters based on spec
        String cusCode = customer.getDigiCode() != null ? customer.getDigiCode() : String.valueOf(customer.getCustomerId());
        if (cusCode.length() > 20) {
            cusCode = cusCode.substring(0, 20);
        }
        request.setCustomerCode(cusCode);
        
        request.setCustomerName(null);
        request.setPhone(null);
        request.setAddress(null);
        request.setBills(bills);


        // Calculate Checksum
        try {
            String jsonRequest = mapper.writeValueAsString(request);
            Map<String, String> parameters = PaybillChecksumUtil.getDicParamFromJson(jsonRequest);
            String checksum = PaybillChecksumUtil.createCheckSum(parameters, props.getPassword());
            request.setChecksum(checksum);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating checksum", e);
        }

        // Call API
        PaybillQrResponse response = paybillQrClient.createQrCode(request);

        if (response.getReturnCode() != 1 && response.getReturnCode() != 2) {
            throw new RuntimeException("Lỗi tạo mã QR Paybill: " + response.getDescription() + " (Mã: " + response.getReturnCode() + ")");
        }

        return new PaybillQrResult(
            response.getQrContent(), 
            response.getQrIndentify(), 
            response.getExpiredDate(), 
            totalAmount, 
            periods
        );
    }

    private String formatPeriod(String yearMonth) {
        if (yearMonth != null && yearMonth.length() == 6) {
            return yearMonth.substring(4, 6) + "/" + yearMonth.substring(0, 4);
        }
        return yearMonth;
    }
}
