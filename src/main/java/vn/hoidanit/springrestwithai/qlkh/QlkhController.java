package vn.hoidanit.springrestwithai.qlkh;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.dto.CustomerLoginRequest;
import vn.hoidanit.springrestwithai.qlkh.dto.CustomerLoginResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.InvoiceResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.TokenResponse;
import vn.hoidanit.springrestwithai.qlkh.entity.Customer;
import vn.hoidanit.springrestwithai.qlkh.entity.MonthInvoice;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/qlkh")
public class QlkhController {

    private final CustomerRepository customerRepository;
    private final MonthInvoiceRepository monthInvoiceRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public QlkhController(CustomerRepository customerRepository,
            MonthInvoiceRepository monthInvoiceRepository,
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder) {
        this.customerRepository = customerRepository;
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    // 1. Đăng nhập bằng mã KH + SĐT
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody CustomerLoginRequest request) {
        Customer customer = customerRepository
                .findByDigiCodeAndPhone(request.digiCode(), request.phone())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "mã KH/SĐT",
                        request.digiCode()));
        String token = generateToken(customer);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", new TokenResponse(token)));
    }

    // 2. Lấy thông tin khách hàng (từ token)
    @GetMapping("/customers/me")
    public ResponseEntity<ApiResponse<CustomerLoginResponse>> getCustomer(
            @RequestHeader("Authorization") String authHeader) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công", toResponse(customer, null)));
    }

    // 3. Lấy danh sách hóa đơn (chỉ của khách hàng đã đăng nhập)
    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getInvoices(
            @RequestHeader("Authorization") String authHeader,
            @ParameterObject Pageable pageable) {
        Customer customer = getCustomerFromToken(authHeader);
        Page<InvoiceResponse> page = monthInvoiceRepository
                .findByCustomerId(customer.getCustomerId(), pageable)
                .map(this::toInvoiceResponse);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hóa đơn thành công",
                ResultPaginationDTO.fromPage(page)));
    }

    // 4. Lấy chi tiết hóa đơn (kiểm tra thuộc về đúng khách hàng)
    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer invoiceId) {
        Customer customer = getCustomerFromToken(authHeader);
        MonthInvoice invoice = monthInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "id", invoiceId));
        if (!invoice.getCustomerId().equals(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Hóa đơn", "id", invoiceId);
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết hóa đơn thành công",
                toInvoiceResponse(invoice)));
    }

    // ========== PRIVATE HELPERS ==========

    private String generateToken(Customer customer) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(customer.getDigiCode())
                .issuedAt(now)
                .expiresAt(now.plusMillis(accessTokenExpiration))
                .claim("customerId", customer.getCustomerId())
                .claim("digiCode", customer.getDigiCode())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
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

    private CustomerLoginResponse toResponse(Customer c, String token) {
        return new CustomerLoginResponse(
                c.getCustomerId(), c.getCode(), c.getDigiCode(), c.getName(), c.getShortName(),
                c.getPhone(), c.getAddress(), c.getEmail(),
                c.getContactName(), c.getContactPhone(), c.getBalance(), c.getStatus(), token);
    }

    private InvoiceResponse toInvoiceResponse(MonthInvoice inv) {
        String label = (inv.getPaymentStatus() != null && inv.getPaymentStatus() == 1)
                ? "Đã thanh toán"
                : "Chưa thanh toán";
        return new InvoiceResponse(
                inv.getMonthInvoiceId(), inv.getCustomerId(), inv.getYearMonth(),
                inv.getAmount(), inv.getEnvFee(), inv.getTaxFee(),
                inv.getInvStatus(), inv.getPaymentStatus(), label,
                inv.getCreatedDate(), inv.getStartDate(), inv.getEndDate(),
                inv.getOldVal(), inv.getNewVal(), inv.getWaterMeterSerial(),
                inv.getNumOfHouseHold());
    }
}
