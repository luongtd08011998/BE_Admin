package vn.hoidanit.springrestwithai.qlkh;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.dto.CustomerLoginRequest;
import vn.hoidanit.springrestwithai.qlkh.dto.CustomerLoginResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.InvoiceResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.SalesInvoiceResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.TokenResponse;
import vn.hoidanit.springrestwithai.qlkh.entity.Customer;
import vn.hoidanit.springrestwithai.qlkh.entity.MonthInvoice;
import vn.hoidanit.springrestwithai.qlkh.entity.SalesInvoice;

@RestController
@RequestMapping("/api/v1/qlkh")
public class QlkhController {

    private final CustomerRepository customerRepository;
    private final MonthInvoiceRepository monthInvoiceRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public QlkhController(CustomerRepository customerRepository,
            MonthInvoiceRepository monthInvoiceRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder) {
        this.customerRepository = customerRepository;
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Bước 1: Đăng nhập bằng mã KH (DigiCode) + SĐT — nhận JWT để gọi các API sau.
     */
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

    /**
     * Thông tin khách hàng đăng nhập (chỉ các trường public theo databaseqlkh.md).
     */
    @GetMapping("/customers/me")
    public ResponseEntity<ApiResponse<CustomerLoginResponse>> getCustomer(
            @RequestHeader("Authorization") String authHeader) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công",
                toCustomerResponse(customer)));
    }

    /**
     * Danh sách hóa đơn theo khách hàng + trạng thái thanh toán; phân trang dạng meta + result.
     */
    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getInvoices(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String yearMonth,
            @ParameterObject Pageable pageable) {
        Customer customer = getCustomerFromToken(authHeader);
        String ym = yearMonth != null ? yearMonth.trim() : "";
        /* Kỳ mới nhất trước (yearMonth DESC), tie-break theo id hóa đơn */
        Pageable pageableSorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("yearMonth"), Sort.Order.desc("monthInvoiceId")));
        Page<MonthInvoice> source = ym.isEmpty()
                ? monthInvoiceRepository.findByCustomerIdExcludingZeroTotal(
                        customer.getCustomerId(), pageableSorted)
                : monthInvoiceRepository.findByCustomerIdAndYearMonthContainingExcludingZeroTotal(
                        customer.getCustomerId(), ym, pageableSorted);
        Page<InvoiceResponse> page = source.map(inv -> toInvoiceResponse(inv, customer));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hóa đơn thành công",
                ResultPaginationDTO.fromPage(page)));
    }

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
                toInvoiceResponse(invoice, customer)));
    }

    /**
     * Danh sách hóa đơn bán (salesinvoice) theo khách hàng; lọc tùy chọn theo mẫu số (templateCode).
     */
    @GetMapping("/sales-invoices")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getSalesInvoices(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String templateCode,
            @ParameterObject Pageable pageable) {
        Customer customer = getCustomerFromToken(authHeader);
        String tc = templateCode != null ? templateCode.trim() : "";
        Pageable pageableSorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("salesInvoiceId")));
        Page<SalesInvoice> source = tc.isEmpty()
                ? salesInvoiceRepository.findByCustomerId(customer.getCustomerId(), pageableSorted)
                : salesInvoiceRepository.findByCustomerIdAndTemplateCodeContaining(
                        customer.getCustomerId(), tc, pageableSorted);
        Page<SalesInvoiceResponse> page = source.map(inv -> toSalesInvoiceResponse(inv, customer));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hóa đơn bán thành công",
                ResultPaginationDTO.fromPage(page)));
    }

    @GetMapping("/sales-invoices/{salesInvoiceId}")
    public ResponseEntity<ApiResponse<SalesInvoiceResponse>> getSalesInvoice(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer salesInvoiceId) {
        Customer customer = getCustomerFromToken(authHeader);
        SalesInvoice invoice = salesInvoiceRepository.findById(salesInvoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn bán", "id", salesInvoiceId));
        if (!invoice.getCustomerId().equals(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Hóa đơn bán", "id", salesInvoiceId);
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết hóa đơn bán thành công",
                toSalesInvoiceResponse(invoice, customer)));
    }

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

    private CustomerLoginResponse toCustomerResponse(Customer c) {
        return new CustomerLoginResponse(
                c.getDigiCode(),
                c.getName(),
                c.getAddress(),
                c.getPhone(),
                c.getEmail(),
                c.getSms(),
                c.getTaxCode(),
                c.getIsActive(),
                c.getIsWaterCut());
    }

    private SalesInvoiceResponse toSalesInvoiceResponse(SalesInvoice inv, Customer customer) {
        return new SalesInvoiceResponse(
                inv.getSalesInvoiceId(),
                inv.getInvoiceNum(),
                inv.getInvoiceDate(),
                inv.getTemplateCode(),
                customer.getDigiCode(),
                customer.getName(),
                inv.getAddress(),
                inv.getInvoiceTotal(),
                inv.getStatus());
    }

    private InvoiceResponse toInvoiceResponse(MonthInvoice inv, Customer customer) {
        Double amount = inv.getAmount();
        Double envFee = inv.getEnvFee();
        Double taxFee = inv.getTaxFee();
        return new InvoiceResponse(
                inv.getMonthInvoiceId(),
                customer.getDigiCode(),
                customer.getName(),
                inv.getYearMonth(),
                inv.getCreatedDate(),
                inv.getNumOfHouseHold(),
                inv.getWaterMeterSerial(),
                amount,
                envFee,
                taxFee,
                totalInvoiceAmount(amount, envFee, taxFee),
                inv.getPaymentStatus(),
                paymentStatusLabel(inv.getPaymentStatus()),
                inv.getOldVal(),
                inv.getNewVal());
    }

    /** Tổng tiền: Amount + EnvFee + TaxFee (null coi như 0). */
    private static double totalInvoiceAmount(Double amount, Double envFee, Double taxFee) {
        double a = amount != null ? amount : 0d;
        double e = envFee != null ? envFee : 0d;
        double t = taxFee != null ? taxFee : 0d;
        return a + e + t;
    }

    /**
     * {@code PaymentStatus} legacy: 1 = chưa thanh toán, 2 = đã thanh toán; null hoặc mã khác → Không xác định.
     */
    private static String paymentStatusLabel(Integer paymentStatus) {
        if (paymentStatus == null) {
            return "Không xác định";
        }
        return switch (paymentStatus) {
            case 1 -> "Chưa thanh toán";
            case 2 -> "Đã thanh toán";
            default -> "Không xác định";
        };
    }
}
