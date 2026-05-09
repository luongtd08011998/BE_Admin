package vn.hoidanit.springrestwithai.qlkh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.dto.AdminInvoiceFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.dto.AdminInvoiceResponse;
import vn.hoidanit.springrestwithai.qlkh.dto.InvoiceViewResponse;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptInvoiceHtmlParser;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalInvoiceClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class InvoiceAdminService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceAdminService.class);

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final VnptPortalInvoiceClient vnptPortalInvoiceClient;
    private final VnptInvoiceHtmlParser vnptInvoiceHtmlParser;

    // Thread pool dùng riêng cho các lần gọi VNPT song song
    private final ExecutorService vnptExecutor = Executors.newFixedThreadPool(10);

    public InvoiceAdminService(MonthInvoiceRepository monthInvoiceRepository,
                               VnptPortalInvoiceClient vnptPortalInvoiceClient,
                               VnptInvoiceHtmlParser vnptInvoiceHtmlParser) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.vnptPortalInvoiceClient = vnptPortalInvoiceClient;
        this.vnptInvoiceHtmlParser = vnptInvoiceHtmlParser;
    }

    public ResultPaginationDTO getAll(AdminInvoiceFilterRequest filter, Pageable pageable) {
        String yearMonth = (filter != null && filter.getYearMonth() != null) ? filter.getYearMonth().trim() : null;

        if (yearMonth == null || yearMonth.isBlank()) {
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                    pageable.getPageNumber() + 1,
                    pageable.getPageSize(),
                    0,
                    0);
            return new ResultPaginationDTO(meta, Collections.emptyList());
        }

        Integer paymentStatus = (filter != null) ? filter.getPaymentStatus() : null;
        String customerName = (filter != null) ? filter.getCustomerName() : null;
        String digiCode = (filter != null) ? filter.getDigiCode() : null;

        Page<AdminInvoiceResponse> page = monthInvoiceRepository.findAdminInvoices(yearMonth, paymentStatus, customerName, digiCode, pageable);

        // Gọi VNPT song song cho tất cả hóa đơn trong trang hiện tại
        List<AdminInvoiceResponse> content = page.getContent();
        fetchInvoiceNosParallel(content);

        return ResultPaginationDTO.fromPage(page);
    }

    /**
     * Gọi VNPT lấy invoiceNo cho tất cả hóa đơn trong danh sách một cách SONG SONG.
     * Thay vì gọi N lần tuần tự (N × 1.5s), ta gọi tất cả cùng lúc (~1.5s tổng).
     */
    private void fetchInvoiceNosParallel(List<AdminInvoiceResponse> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return;
        }

        // Tạo danh sách CompletableFuture, mỗi cái gọi VNPT cho 1 hóa đơn
        List<CompletableFuture<Void>> futures = invoices.stream()
                .filter(res -> res.getFkey() != null && !res.getFkey().isBlank())
                .map(res -> CompletableFuture.runAsync(() -> {
                    String vnptFkey = normalizeVnptFkey(res.getFkey());
                    try {
                        String payload = vnptPortalInvoiceClient.getInvView(vnptFkey);
                        if (payload != null && !payload.startsWith("ERR:")) {
                            InvoiceViewResponse dto = vnptInvoiceHtmlParser.parse(payload, "UNKNOWN");
                            res.setInvoiceNo(dto.invoiceNo());
                        } else {
                            log.warn("VNPT Portal returned error for fkey {}: {}", res.getFkey(), payload);
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse VNPT invoice HTML for fkey {}: {}", res.getFkey(), e.getMessage());
                    }
                }, vnptExecutor))
                .toList();

        // Chờ tất cả hoàn thành (timeout tối đa 30 giây)
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Timeout hoặc lỗi khi gọi VNPT song song: {}", e.getMessage());
        }
    }

    private static String normalizeVnptFkey(String rawFkey) {
        String t = rawFkey != null ? rawFkey.trim() : "";
        if (t.isEmpty()) {
            return t;
        }
        return t.contains(".") ? t : ("CNTOCTIEN." + t);
    }
}

