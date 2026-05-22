package vn.hoidanit.springrestwithai.qlkh.invoice;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.*;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalInvoiceClient.VnptDebugResult;

@RestController
@RequestMapping("/api/v1/qlkh")
public class QlkhInvoiceController {

    private final QlkhInvoiceService qlkhInvoiceService;
    private final CustomerRepository customerRepository;
    private final JwtDecoder jwtDecoder;

    public QlkhInvoiceController(QlkhInvoiceService qlkhInvoiceService,
                                 CustomerRepository customerRepository,
                                 JwtDecoder jwtDecoder) {
        this.qlkhInvoiceService = qlkhInvoiceService;
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

    @GetMapping("/vnpt/health")
    public ResponseEntity<ApiResponse<VnptDebugResult>> vnptHealth(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String fkey) {
        getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra VNPT PortalService thành công", 
                qlkhInvoiceService.vnptHealth(fkey)));
    }

    @GetMapping("/month-invoices/readings")
    public ResponseEntity<ApiResponse<List<MonthInvoiceReadingItemResponse>>> getMonthInvoiceReadings(
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String fromYearMonth,
            @RequestParam(required = false) String toYearMonth) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", 
                qlkhInvoiceService.getMonthInvoiceReadings(yearMonth, fromYearMonth, toYearMonth)));
    }

    @GetMapping("/month-invoices/consumption-history")
    public ResponseEntity<ApiResponse<List<ConsumptionHistoryItemResponse>>> getConsumptionHistory(
            @RequestParam String customerCode,
            @RequestParam String fromYearMonth,
            @RequestParam String toYearMonth) {
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tiêu thụ thành công", 
                qlkhInvoiceService.getConsumptionHistory(customerCode, fromYearMonth, toYearMonth)));
    }

    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getInvoices(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String yearMonth,
            @ParameterObject Pageable pageable) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hóa đơn thành công",
                qlkhInvoiceService.getInvoices(customer, yearMonth, pageable)));
    }

    @GetMapping("/invoices/by-fkey")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByFkey(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String fkey) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Lấy hóa đơn theo fkey thành công",
                qlkhInvoiceService.getInvoiceByFkey(customer, fkey)));
    }

    @GetMapping("/invoices/by-root-key")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByRootKey(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String rootKey) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Lấy hóa đơn theo rootKey thành công",
                qlkhInvoiceService.getInvoiceByRootKey(customer, rootKey)));
    }

    @GetMapping("/invoices/{invoiceId}/e-invoice-download")
    public ResponseEntity<byte[]> downloadMonthEInvoiceXml(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer invoiceId) {
        Customer customer = getCustomerFromToken(authHeader);
        QlkhInvoiceService.EInvoiceDownloadDto dto = qlkhInvoiceService.downloadMonthEInvoiceXml(customer, invoiceId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(dto.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(dto.isZip() ? MediaType.parseMediaType("application/zip") : MediaType.APPLICATION_XML)
                .body(dto.content());
    }

    @GetMapping("/invoices/{invoiceId}/e-invoice-view")
    public ResponseEntity<ApiResponse<InvoiceViewResponse>> viewMonthEInvoice(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer invoiceId) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success(
                qlkhInvoiceService.viewMonthEInvoice(customer, invoiceId)));
    }

    @GetMapping("/invoices/e-invoice-list")
    public ResponseEntity<byte[]> listEInvoices(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        Customer customer = getCustomerFromToken(authHeader);
        byte[] body = qlkhInvoiceService.listEInvoices(customer, fromDate, toDate);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(body);
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer invoiceId) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết hóa đơn thành công",
                qlkhInvoiceService.getInvoice(customer, invoiceId)));
    }

    @GetMapping("/sales-invoices")
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getSalesInvoices(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String templateCode,
            @ParameterObject Pageable pageable) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hóa đơn bán thành công",
                qlkhInvoiceService.getSalesInvoices(customer, templateCode, pageable)));
    }

    @GetMapping("/sales-invoices/{salesInvoiceId}")
    public ResponseEntity<ApiResponse<SalesInvoiceResponse>> getSalesInvoice(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer salesInvoiceId) {
        Customer customer = getCustomerFromToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết hóa đơn bán thành công",
                qlkhInvoiceService.getSalesInvoice(customer, salesInvoiceId)));
    }
}
