package vn.hoidanit.springrestwithai.qlkh;

import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.dto.CustomerLoginRequest;
import vn.hoidanit.springrestwithai.qlkh.dto.CustomerLoginResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.InvoiceResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.MonthInvoiceReadingItemResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.SalesInvoiceResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.TokenResponse;
import vn.hoidanit.springrestwithai.qlkh.entity.Customer;
import vn.hoidanit.springrestwithai.qlkh.entity.MonthInvoice;
import vn.hoidanit.springrestwithai.qlkh.entity.SalesInvoice;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalInvoiceClient;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalInvoiceClient.VnptDebugResult;

@RestController
@RequestMapping("/api/v1/qlkh")
public class QlkhController {

    private final CustomerRepository customerRepository;
    private final MonthInvoiceRepository monthInvoiceRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final VnptPortalInvoiceClient vnptPortalInvoiceClient;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("^\\d{6}$");

    public QlkhController(CustomerRepository customerRepository,
            MonthInvoiceRepository monthInvoiceRepository,
            SalesInvoiceRepository salesInvoiceRepository,
            VnptPortalInvoiceClient vnptPortalInvoiceClient,
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder) {
        this.customerRepository = customerRepository;
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.vnptPortalInvoiceClient = vnptPortalInvoiceClient;
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
     * Debug nhanh kết nối VNPT PortalService (SOAP) theo fkey.
     * Endpoint này yêu cầu JWT QLKH để tránh bị lạm dụng brute-force.
     */
    @GetMapping("/vnpt/health")
    public ResponseEntity<ApiResponse<VnptDebugResult>> vnptHealth(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String fkey) {
        // Chỉ cần verify token hợp lệ.
        getCustomerFromToken(authHeader);
        String key = fkey != null ? fkey.trim() : "";
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Tham số fkey không được để trống");
        }
        VnptDebugResult res = vnptPortalInvoiceClient.debugDownloadInvZipFkey(normalizeVnptFkey(key), false);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra VNPT PortalService thành công", res));
    }

    /**
     * Chỉ số đồng hồ (OldVal/NewVal) theo một kỳ {@code yearMonth} hoặc khoảng kỳ (YYYYMM). Không yêu cầu JWT.
     */
    @GetMapping("/month-invoices/readings")
    public ResponseEntity<ApiResponse<List<MonthInvoiceReadingItemResponse>>> getMonthInvoiceReadings(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String fromYearMonth,
            @RequestParam(required = false) String toYearMonth) {
        String ym = trimEmptyToNull(yearMonth);
        String from = trimEmptyToNull(fromYearMonth);
        String to = trimEmptyToNull(toYearMonth);
        boolean hasYm = ym != null;
        boolean hasFrom = from != null;
        boolean hasTo = to != null;

        if (hasYm && !hasFrom && !hasTo) {
            requireValidYearMonth(ym, "yearMonth");
            List<MonthInvoiceReadingItemResponse> rows = monthInvoiceRepository.findReadingsByYearMonth(ym);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", rows));
        }
        if (hasFrom && hasTo && !hasYm) {
            requireValidYearMonth(from, "fromYearMonth");
            requireValidYearMonth(to, "toYearMonth");
            if (from.compareTo(to) > 0) {
                throw new IllegalArgumentException("fromYearMonth phải nhỏ hơn hoặc bằng toYearMonth");
            }
            List<MonthInvoiceReadingItemResponse> rows =
                    monthInvoiceRepository.findReadingsByYearMonthRange(from, to);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", rows));
        }
        throw new IllegalArgumentException(
                "Chỉ định yearMonth=YYYYMM hoặc fromYearMonth=YYYYMM và toYearMonth=YYYYMM (đủ 6 chữ số)");
    }

    /**
     * Danh sách hóa đơn theo khách hàng + trạng thái thanh toán; phân trang dạng meta + result.
     * Tra theo {@code Fkey}/{@code RootKey} dùng {@link #getInvoiceByFkey} / {@link #getInvoiceByRootKey}.
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

    /**
     * Tra cứu một hóa đơn theo {@code Fkey} (VNPT) — chỉ trả về nếu thuộc khách đang đăng nhập.
     */
    @GetMapping("/invoices/by-fkey")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByFkey(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String fkey) {
        Customer customer = getCustomerFromToken(authHeader);
        String key = fkey != null ? fkey.trim() : "";
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Tham số fkey không được để trống");
        }
        MonthInvoice invoice = monthInvoiceRepository
                .findByCustomerIdAndFkey(customer.getCustomerId(), key)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "fkey", key));
        return ResponseEntity.ok(ApiResponse.success("Lấy hóa đơn theo fkey thành công",
                toInvoiceResponse(invoice, customer)));
    }

    /**
     * Tra cứu một hóa đơn theo {@code RootKey} — chỉ trả về nếu thuộc khách đang đăng nhập.
     */
    @GetMapping("/invoices/by-root-key")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByRootKey(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String rootKey) {
        Customer customer = getCustomerFromToken(authHeader);
        String key = rootKey != null ? rootKey.trim() : "";
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Tham số rootKey không được để trống");
        }
        MonthInvoice invoice = monthInvoiceRepository
                .findByCustomerIdAndRootKey(customer.getCustomerId(), key)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "rootKey", key));
        return ResponseEntity.ok(ApiResponse.success("Lấy hóa đơn theo rootKey thành công",
                toInvoiceResponse(invoice, customer)));
    }

    /**
     * Tải file XML hóa đơn điện tử từ VNPT (SOAP {@code downloadInvErrorFkey}) — chỉ khi hóa đơn thuộc khách
     * và đã có {@code Fkey}.
     */
    @GetMapping("/invoices/{invoiceId}/e-invoice-download")
    public ResponseEntity<byte[]> downloadMonthEInvoiceXml(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer invoiceId) {
        Customer customer = getCustomerFromToken(authHeader);
        MonthInvoice invoice = monthInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "id", invoiceId));
        if (!invoice.getCustomerId().equals(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Hóa đơn", "id", invoiceId);
        }
        String fkey = invoice.getFkey();
        if (fkey == null || fkey.isBlank()) {
            throw new IllegalArgumentException(
                    "Hóa đơn chưa có mã Fkey — chưa thể tải từ hệ thống hóa đơn điện tử.");
        }
        String vnptFkey = normalizeVnptFkey(fkey.trim());
        String payload = vnptPortalInvoiceClient.downloadInvZipFkey(vnptFkey, false);
        if (payload.startsWith("ERR:")) {
            throw new IllegalArgumentException("VNPT: " + payload.trim());
        }
        String ymSafe = safeFilenameSegment(invoice.getYearMonth());
        // downloadInvZipFkey thường trả base64 của file zip. Nếu decode được và đúng magic 'PK' thì trả zip.
        byte[] body = tryDecodeZipBase64(payload);
        boolean isZip = body != null && body.length >= 2 && body[0] == 'P' && body[1] == 'K';
        if (!isZip) {
            body = payload.getBytes(StandardCharsets.UTF_8);
        }
        String ext = isZip ? "zip" : "xml";
        String filename = "hoadon-tien-nuoc-" + ymSafe + "-" + invoiceId + "." + ext;
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(isZip ? MediaType.parseMediaType("application/zip") : MediaType.APPLICATION_XML)
                .body(body);
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
                inv.getNewVal(),
                inv.getRootKey(),
                inv.getFkey());
    }

    private static String trimEmptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static void requireValidYearMonth(String value, String paramName) {
        if (!YEAR_MONTH_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(paramName + " phải là YYYYMM (6 chữ số), ví dụ 202501");
        }
    }

    /** Chỉ giữ ký tự an toàn cho tên file đính kèm (tránh ký tự đặc biệt / xuống dòng từ DB). */
    private static String safeFilenameSegment(String raw) {
        if (raw == null || raw.isBlank()) {
            return "unknown";
        }
        String s = raw.trim().replaceAll("[^a-zA-Z0-9._-]+", "-");
        return s.isEmpty() ? "unknown" : s;
    }

    /**
     * VNPT yêu cầu fkey dạng TenantPrefix.Fkey, ví dụ {@code CNTOCTIEN.<Fkey>}.
     * Nếu DB đã lưu đúng format (có dấu '.'), giữ nguyên.
     */
    private static String normalizeVnptFkey(String rawFkey) {
        String t = rawFkey != null ? rawFkey.trim() : "";
        if (t.isEmpty()) {
            return t;
        }
        return t.contains(".") ? t : ("CNTOCTIEN." + t);
    }

    private static byte[] tryDecodeZipBase64(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty() || t.startsWith("<")) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(t);
        } catch (IllegalArgumentException ex) {
            return null;
        }
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
