package vn.hoidanit.springrestwithai.qlkh.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cron Job quét hóa đơn mới theo ngày và gửi Push Notification tự động.
 *
 * <p>Chạy mỗi 5 phút (cấu hình qua {@code app.invoice-notify.cron}).
 * Logic:
 * <ol>
 *   <li>Dùng Pagination lấy toàn bộ hóa đơn của ngày hôm đó (từng trang 100 dòng).</li>
 *   <li>So sánh in-memory với danh sách đã gửi ({@code notified_invoice}) — tự động bỏ qua hóa đơn đã gửi.</li>
 *   <li>Nếu chưa → gửi Push FCM + lưu dấu đã gửi.</li>
 *   <li>Tiếp tục vòng lặp đến khi hết hóa đơn của ngày đó.</li>
 * </ol>
 */
@Component
public class InvoiceNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceNotificationScheduler.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_ACTIVITY");
    private static final int BATCH_SIZE = 100;
    private final ExecutorService notifyExecutor = Executors.newFixedThreadPool(10);

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final NotificationService notificationService;
    private final vn.hoidanit.springrestwithai.feature.notification.NotifiedInvoiceRepository notifiedInvoiceRepository;
    private final InvoiceNotificationScheduler self;

    public InvoiceNotificationScheduler(MonthInvoiceRepository monthInvoiceRepository,
                                        NotificationService notificationService,
                                        vn.hoidanit.springrestwithai.feature.notification.NotifiedInvoiceRepository notifiedInvoiceRepository,
                                        @Lazy InvoiceNotificationScheduler self) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.notificationService = notificationService;
        this.notifiedInvoiceRepository = notifiedInvoiceRepository;
        this.self = self;
    }

    @Scheduled(cron = "${app.invoice-notify.cron:0 */5 * * * *}", zone = "Asia/Ho_Chi_Minh")
    public void checkAndNotifyNewInvoices() {
        checkAndNotifyNewInvoices(null);
    }

    /**
     * Kích hoạt thủ công với ngày tùy chỉnh.
     * @param datePrefix Prefix ngày để query (ví dụ "2026-04-23", "2026-04", "20260423").
     *                   Nếu null → dùng ngày hôm nay định dạng "yyyyMMdd".
     */
    public void checkAndNotifyNewInvoices(String datePrefix) {
        String queryDate = (datePrefix != null && !datePrefix.isBlank())
                ? datePrefix.trim()
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        log.info("[InvoiceNotify] Bắt đầu quét hóa đơn với datePrefix='{}'", queryDate);

        AtomicInteger sentCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);
        int page = 0;

        while (true) {
            List<InvoiceInfoDTO> invoices = self.fetchNewInvoices(queryDate, page, BATCH_SIZE);
            if (invoices.isEmpty()) {
                break;
            }

            log.info("[InvoiceNotify] Page {}: Tìm thấy {} hóa đơn.", page, invoices.size());

            Set<Integer> notifiedSet = self.fetchAlreadyNotifiedIds(
                    invoices.stream().map(InvoiceInfoDTO::getMonthInvoiceId).toList());
            skipCount.addAndGet(notifiedSet.size());

            List<CompletableFuture<InvoiceInfoDTO>> futures = invoices.stream()
                    .map(invoice -> CompletableFuture.supplyAsync(() -> {
                        if (notifiedSet.contains(invoice.getMonthInvoiceId())) {
                            return invoice;
                        }
                        try {
                            boolean sent = notificationService.sendAndMarkInvoiceNotification(
                                    invoice.getMonthInvoiceId(),
                                    invoice.getCustomerId(),
                                    invoice.getYearMonth(),
                                    invoice.getDigiCode(),
                                    invoice.getCustomerName(),
                                    invoice.getAmount(),
                                    invoice.getAddress()
                            );
                            if (sent) sentCount.incrementAndGet();
                        } catch (Exception e) {
                            log.error("[InvoiceNotify] Lỗi khi gửi thông báo cho invoiceId={}: {}",
                                    invoice.getMonthInvoiceId(), e.getMessage(), e);
                            return null;
                        }
                        return invoice;
                    }, notifyExecutor))
                    .toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(120, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("[InvoiceNotify] Timeout khi gửi thông báo song song: {}", e.getMessage());
            }

            page++;
        }

        if (sentCount.get() > 0) {
            String msg = String.format("[InvoiceNotify] Hoàn tất quét '%s': gửi mới=%d, bỏ qua=%d",
                    queryDate, sentCount.get(), skipCount.get());
            log.info(msg);
            businessLogger.info("TYPE: NOTIFICATION_SUMMARY | ACTION: INVOICE_NOTIFY | STATUS: SUCCESS | MSG: {}", msg);
        } else {
            log.info("[InvoiceNotify] Hoàn tất: không có thông báo mới nào.");
        }
    }

    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public List<InvoiceInfoDTO> fetchNewInvoices(String datePrefix, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return monthInvoiceRepository.findNewInvoicesByDatePrefix(datePrefix, pageable);
    }

    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public Set<Integer> fetchAlreadyNotifiedIds(List<Integer> allIds) {
        List<Integer> notifiedIds = notifiedInvoiceRepository.findNotifiedInvoiceIds(allIds);
        return new java.util.HashSet<>(notifiedIds);
    }
}
