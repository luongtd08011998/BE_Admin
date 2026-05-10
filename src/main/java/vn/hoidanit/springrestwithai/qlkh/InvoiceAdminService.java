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

import vn.hoidanit.springrestwithai.qlkh.dto.DebtReminderResponse;
import vn.hoidanit.springrestwithai.qlkh.entity.MonthInvoice;

@Service
public class InvoiceAdminService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceAdminService.class);

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final VnptPortalInvoiceClient vnptPortalInvoiceClient;
    private final VnptInvoiceHtmlParser vnptInvoiceHtmlParser;
    private final NotificationService notificationService;

    // Thread pool dùng riêng cho các lần gọi VNPT song song
    private final ExecutorService vnptExecutor = Executors.newFixedThreadPool(10);
    // Thread pool dùng cho gửi notification song song
    private final ExecutorService notificationExecutor = Executors.newFixedThreadPool(10);

    public InvoiceAdminService(MonthInvoiceRepository monthInvoiceRepository,
                               VnptPortalInvoiceClient vnptPortalInvoiceClient,
                               VnptInvoiceHtmlParser vnptInvoiceHtmlParser,
                               NotificationService notificationService) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.vnptPortalInvoiceClient = vnptPortalInvoiceClient;
        this.vnptInvoiceHtmlParser = vnptInvoiceHtmlParser;
        this.notificationService = notificationService;
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
        Integer remindStatus = (filter != null) ? filter.getRemindStatus() : null;

        // Fetch all reminded invoice IDs from Notification table
        java.util.List<Long> remindedLongIds = notificationService.findAllRemindedInvoiceIds();
        java.util.List<Integer> remindedIds = remindedLongIds.stream().map(Long::intValue).toList();

        // If list is empty, avoid SQL syntax errors in IN clause
        if (remindedIds.isEmpty()) {
            remindedIds = java.util.List.of(-1);
        }

        Page<AdminInvoiceResponse> page = monthInvoiceRepository.findAdminInvoices(yearMonth, paymentStatus, customerName, digiCode, remindStatus, remindedIds, pageable);

        // Gọi VNPT song song cho tất cả hóa đơn trong trang hiện tại
        List<AdminInvoiceResponse> content = page.getContent();
        
        // Map isReminded boolean
        java.util.Set<Integer> remindedSet = new java.util.HashSet<>(remindedIds);
        for (AdminInvoiceResponse res : content) {
            if (remindedSet.contains(res.getId())) {
                res.setIsReminded(true);
            } else {
                res.setIsReminded(false);
            }
        }

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

    public DebtReminderResponse sendDebtReminder(String yearMonth, Integer monthInvoiceId) {
        if (yearMonth == null || yearMonth.isBlank()) {
            throw new IllegalArgumentException("yearMonth is required");
        }

        List<vn.hoidanit.springrestwithai.qlkh.dto.InvoiceInfoDTO> unpaidInvoices = monthInvoiceRepository.findUnpaidInvoiceDTOsByYearMonth(yearMonth.trim());
        
        if (monthInvoiceId != null) {
            unpaidInvoices = unpaidInvoices.stream()
                .filter(inv -> inv.getMonthInvoiceId().equals(monthInvoiceId))
                .toList();
        }

        if (unpaidInvoices == null || unpaidInvoices.isEmpty()) {
            return new DebtReminderResponse(0, 0);
        }

        java.util.concurrent.atomic.AtomicInteger sentCount = new java.util.concurrent.atomic.AtomicInteger(0);

        List<CompletableFuture<Void>> futures = unpaidInvoices.stream()
                .filter(inv -> inv.getCustomerId() != null)
                .map(inv -> CompletableFuture.runAsync(() -> {
                    try {
                        notificationService.sendDebtReminderNotification(
                                inv.getCustomerId(),
                                inv.getMonthInvoiceId(),
                                inv.getYearMonth(),
                                inv.getDigiCode(),
                                inv.getCustomerName(),
                                inv.getAmount());
                        sentCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("Lỗi khi gửi thông báo nhắc nợ cho invoiceId={}: {}", inv.getMonthInvoiceId(), e.getMessage());
                    }
                }, notificationExecutor))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Timeout hoặc lỗi khi gửi thông báo nhắc nợ song song: {}", e.getMessage());
        }

        return new DebtReminderResponse(sentCount.get(), unpaidInvoices.size() - sentCount.get());
    }

    private static String normalizeVnptFkey(String rawFkey) {
        String t = rawFkey != null ? rawFkey.trim() : "";
        if (t.isEmpty()) {
            return t;
        }
        return t.contains(".") ? t : ("CNTOCTIEN." + t);
    }
}

