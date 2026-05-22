package vn.hoidanit.springrestwithai.qlkh.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.*;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptInvoiceHtmlParser;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalInvoiceClient;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalInvoiceClient.VnptDebugResult;

@Service
public class QlkhInvoiceServiceImpl implements QlkhInvoiceService {

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final VnptPortalInvoiceClient vnptPortalInvoiceClient;
    private final VnptInvoiceHtmlParser vnptInvoiceHtmlParser;

    private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("^\\d{6}$");

    public QlkhInvoiceServiceImpl(MonthInvoiceRepository monthInvoiceRepository,
                                  SalesInvoiceRepository salesInvoiceRepository,
                                  VnptPortalInvoiceClient vnptPortalInvoiceClient,
                                  VnptInvoiceHtmlParser vnptInvoiceHtmlParser) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.vnptPortalInvoiceClient = vnptPortalInvoiceClient;
        this.vnptInvoiceHtmlParser = vnptInvoiceHtmlParser;
    }

    @Override
    public VnptDebugResult vnptHealth(String fkey) {
        String key = fkey != null ? fkey.trim() : "";
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Tham số fkey không được để trống");
        }
        return vnptPortalInvoiceClient.debugDownloadInvZipFkey(normalizeVnptFkey(key), false);
    }

    @Override
    public List<MonthInvoiceReadingItemResponse> getMonthInvoiceReadings(String yearMonth, String fromYearMonth, String toYearMonth) {
        String ym = trimEmptyToNull(yearMonth);
        String from = trimEmptyToNull(fromYearMonth);
        String to = trimEmptyToNull(toYearMonth);
        boolean hasYm = ym != null;
        boolean hasFrom = from != null;
        boolean hasTo = to != null;

        if (hasYm && !hasFrom && !hasTo) {
            requireValidYearMonth(ym, "yearMonth");
            return monthInvoiceRepository.findReadingsByYearMonth(ym);
        }
        if (hasFrom && hasTo && !hasYm) {
            requireValidYearMonth(from, "fromYearMonth");
            requireValidYearMonth(to, "toYearMonth");
            if (from.compareTo(to) > 0) {
                throw new IllegalArgumentException("fromYearMonth phải nhỏ hơn hoặc bằng toYearMonth");
            }
            return monthInvoiceRepository.findReadingsByYearMonthRange(from, to);
        }
        throw new IllegalArgumentException(
                "Chỉ định yearMonth=YYYYMM hoặc fromYearMonth=YYYYMM và toYearMonth=YYYYMM (đủ 6 chữ số)");
    }

    @Override
    public List<ConsumptionHistoryItemResponse> getConsumptionHistory(String customerCode, String fromYearMonth, String toYearMonth) {
        String code = customerCode != null ? customerCode.trim() : "";
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Tham số customerCode không được để trống");
        }
        requireValidYearMonth(fromYearMonth, "fromYearMonth");
        requireValidYearMonth(toYearMonth, "toYearMonth");
        if (fromYearMonth.compareTo(toYearMonth) > 0) {
            throw new IllegalArgumentException("fromYearMonth phải nhỏ hơn hoặc bằng toYearMonth");
        }
        return monthInvoiceRepository.findConsumptionHistory(code, fromYearMonth, toYearMonth);
    }

    @Override
    public ResultPaginationDTO getInvoices(Customer customer, String yearMonth, Pageable pageable) {
        String ym = yearMonth != null ? yearMonth.trim() : "";
        Pageable pageableSorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.desc("yearMonth"), Sort.Order.desc("monthInvoiceId")));
        Page<MonthInvoice> source = ym.isEmpty()
                ? monthInvoiceRepository.findByCustomerId(
                        customer.getCustomerId(), pageableSorted)
                : monthInvoiceRepository.findByCustomerIdAndYearMonthContaining(
                        customer.getCustomerId(), ym, pageableSorted);
        Page<InvoiceResponse> page = source.map(inv -> toInvoiceResponse(inv, customer));
        return ResultPaginationDTO.fromPage(page);
    }

    @Override
    public InvoiceResponse getInvoiceByFkey(Customer customer, String fkey) {
        String key = fkey != null ? fkey.trim() : "";
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Tham số fkey không được để trống");
        }
        MonthInvoice invoice = monthInvoiceRepository
                .findByCustomerIdAndFkey(customer.getCustomerId(), key)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "fkey", key));
        return toInvoiceResponse(invoice, customer);
    }

    @Override
    public InvoiceResponse getInvoiceByRootKey(Customer customer, String rootKey) {
        String key = rootKey != null ? rootKey.trim() : "";
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Tham số rootKey không được để trống");
        }
        MonthInvoice invoice = monthInvoiceRepository
                .findByCustomerIdAndRootKey(customer.getCustomerId(), key)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "rootKey", key));
        return toInvoiceResponse(invoice, customer);
    }

    @Override
    public EInvoiceDownloadDto downloadMonthEInvoiceXml(Customer customer, Integer invoiceId) {
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
        byte[] body = tryDecodeZipBase64(payload);
        boolean isZip = body != null && body.length >= 2 && body[0] == 'P' && body[1] == 'K';
        if (!isZip) {
            body = payload.getBytes(StandardCharsets.UTF_8);
        }
        String ext = isZip ? "zip" : "xml";
        String filename = "hoadon-tien-nuoc-" + ymSafe + "-" + invoiceId + "." + ext;
        return new EInvoiceDownloadDto(body, filename, isZip);
    }

    @Override
    public InvoiceViewResponse viewMonthEInvoice(Customer customer, Integer invoiceId) {
        MonthInvoice invoice = monthInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "id", invoiceId));
        if (!invoice.getCustomerId().equals(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Hóa đơn", "id", invoiceId);
        }
        String fkey = invoice.getFkey();
        if (fkey == null || fkey.isBlank()) {
            throw new IllegalArgumentException(
                    "Hóa đơn chưa có mã Fkey — chưa thể xem từ hệ thống hóa đơn điện tử.");
        }
        String vnptFkey = normalizeVnptFkey(fkey.trim());
        String payload = vnptPortalInvoiceClient.getInvView(vnptFkey);
        if (payload.startsWith("ERR:")) {
            throw new IllegalArgumentException("VNPT: " + payload.trim());
        }
        String status = payload.contains("CHƯA THANH TOÁN") ? "UNPAID" : "PAID";
        return vnptInvoiceHtmlParser.parse(payload, status);
    }

    @Override
    public byte[] listEInvoices(Customer customer, String fromDate, String toDate) {
        String cusCode = customer.getDigiCode();
        String payload = vnptPortalInvoiceClient.listInvByCus(cusCode, fromDate, toDate);
        if (payload.startsWith("ERR:")) {
            throw new IllegalArgumentException("VNPT: " + payload.trim());
        }
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public InvoiceResponse getInvoice(Customer customer, Integer invoiceId) {
        MonthInvoice invoice = monthInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", "id", invoiceId));
        if (!invoice.getCustomerId().equals(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Hóa đơn", "id", invoiceId);
        }
        return toInvoiceResponse(invoice, customer);
    }

    @Override
    public ResultPaginationDTO getSalesInvoices(Customer customer, String templateCode, Pageable pageable) {
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
        return ResultPaginationDTO.fromPage(page);
    }

    @Override
    public SalesInvoiceResponse getSalesInvoice(Customer customer, Integer salesInvoiceId) {
        SalesInvoice invoice = salesInvoiceRepository.findById(salesInvoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn bán", "id", salesInvoiceId));
        if (!invoice.getCustomerId().equals(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Hóa đơn bán", "id", salesInvoiceId);
        }
        return toSalesInvoiceResponse(invoice, customer);
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
                inv.getFkey(),
                inv.getBlankNo());
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

    private static String safeFilenameSegment(String raw) {
        if (raw == null || raw.isBlank()) {
            return "unknown";
        }
        String s = raw.trim().replaceAll("[^a-zA-Z0-9._-]+", "-");
        return s.isEmpty() ? "unknown" : s;
    }

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

    private static double totalInvoiceAmount(Double amount, Double envFee, Double taxFee) {
        double a = amount != null ? amount : 0d;
        double e = envFee != null ? envFee : 0d;
        double t = taxFee != null ? taxFee : 0d;
        return a + e + t;
    }

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
