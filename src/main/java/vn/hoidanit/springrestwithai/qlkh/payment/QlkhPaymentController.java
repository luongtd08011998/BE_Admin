package vn.hoidanit.springrestwithai.qlkh.payment;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;

@RestController
@RequestMapping("/api/v1/qlkh")
public class QlkhPaymentController {

    private final PaymentLineService paymentLineService;
    private final CustomerRepository customerRepository;
    private final JwtDecoder jwtDecoder;

    public QlkhPaymentController(PaymentLineService paymentLineService,
                                 CustomerRepository customerRepository,
                                 JwtDecoder jwtDecoder) {
        this.paymentLineService = paymentLineService;
        this.customerRepository = customerRepository;
        this.jwtDecoder = jwtDecoder;
    }

    private Customer getCustomerFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        Jwt jwt = jwtDecoder.decode(token);
        String digiCode = jwt.getClaimAsString("digiCode");
        return customerRepository.findByDigiCode(digiCode)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "digiCode", digiCode));
    }

    /**
     * API dành cho Khách hàng: Lấy lịch sử nộp tiền/thanh toán của chính họ.
     * GET /api/v1/qlkh/payments/my-history
     */
    @GetMapping("/payments/my-history")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getMyHistory(
            @RequestHeader("Authorization") String authHeader,
            @ParameterObject Pageable pageable) {
        Customer customer = getCustomerFromToken(authHeader);
        ResultPaginationDTO history = paymentLineService.getPaymentHistory(customer, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử thanh toán thành công", history));
    }

    /**
     * API dành cho Admin: Lấy danh sách toàn bộ các dòng giao dịch thanh toán trong hệ thống.
     * GET /api/v1/qlkh/admin/payments/lines
     */
    @GetMapping("/admin/payments/lines")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAdminPayments(
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) String yearMonth,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = paymentLineService.getAdminPaymentLines(customerId, yearMonth, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách giao dịch thanh toán thành công", result));
    }
}
