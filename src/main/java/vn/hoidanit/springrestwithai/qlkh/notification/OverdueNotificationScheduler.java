package vn.hoidanit.springrestwithai.qlkh.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.NotificationRepository;
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
 * Cron Job quét hóa đơn quá hạn chưa thanh toán và gửi Push Notification (OVERDUE) tự động.
 *
 * <p>Chạy vào lúc 6h00 sáng ngày 19 hàng tháng (cấu hình qua {@code app.overdue-notify.cron}).
 * Logic:
 * <ol>
 *   <li>Mặc định quét hóa đơn của kỳ tháng trước (VD: ngày 19/10 quét kỳ 202609 phát hành đầu tháng 10).</li>
 *   <li>Phân trang lấy các hóa đơn chưa thanh toán (paymentStatus = 1, amount > 0, không có hóa đơn thay thế).</li>
 *   <li>Kiểm tra nếu hóa đơn đã từng được gửi thông báo OVERDUE trước đó thì bỏ qua (chỉ gửi 1 lần duy nhất).</li>
 *   <li>Gửi thông báo Push FCM và lưu Notification type = OVERDUE.</li>
 * </ol>
 */
@Component
public class OverdueNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueNotificationScheduler.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_ACTIVITY");
    private static final int BATCH_SIZE = 100;
    private final ExecutorService notifyExecutor = Executors.newFixedThreadPool(10);

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final OverdueNotificationScheduler self;

    public OverdueNotificationScheduler(MonthInvoiceRepository monthInvoiceRepository,
                                      NotificationService notificationService,
                                      NotificationRepository notificationRepository,
                                      @Lazy OverdueNotificationScheduler self) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.self = self;
    }

    /**
     * Tự động chạy lúc 6:00 sáng ngày 19 hàng tháng (múi giờ Asia/Ho_Chi_Minh).
     */
    @Scheduled(cron = "${app.overdue-notify.cron:0 0 6 19 * *}", zone = "Asia/Ho_Chi_Minh")
    public void checkAndNotifyOverdueInvoices() {
        checkAndNotifyOverdueInvoices(null);
    }

    /**
     * Kích hoạt quét thông báo quá hạn với kỳ tùy chỉnh.
     * @param targetYearMonth Kỳ hóa đơn YYYYMM (ví dụ "202609").
     *                        Nếu null -> mặc định lấy kỳ tháng trước (LocalDate.now().minusMonths(1)).
     */
    public void checkAndNotifyOverdueInvoices(String targetYearMonth) {
        String queryYm = (targetYearMonth != null && !targetYearMonth.isBlank())
                ? targetYearMonth.trim()
                : LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));

        log.info("[OverdueNotify] Bắt đầu quét hóa đơn quá hạn cho kỳ yearMonth='{}'", queryYm);

        AtomicInteger sentCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);
        int page = 0;

        while (true) {
            List<InvoiceInfoDTO> unpaidInvoices = self.fetchUnpaidInvoices(queryYm, page, BATCH_SIZE);
            if (unpaidInvoices == null || unpaidInvoices.isEmpty()) {
                break;
            }

            // Lọc hóa đơn có tiền > 0 và không có hóa đơn thay thế
            List<InvoiceInfoDTO> validInvoices = unpaidInvoices.stream()
                    .filter(inv -> inv.getAmount() != null && inv.getAmount() > 0
                            && (inv.getHasReplacement() == null || !inv.getHasReplacement()))
                    .filter(inv -> inv.getCustomerId() != null)
                    .toList();

            if (validInvoices.isEmpty()) {
                page++;
                continue;
            }

            List<Long> invoiceIds = validInvoices.stream()
                    .map(inv -> Long.valueOf(inv.getMonthInvoiceId()))
                    .toList();

            Set<Long> alreadyOverdueSet = self.fetchAlreadyOverdueIds(invoiceIds);
            skipCount.addAndGet(alreadyOverdueSet.size());

            List<CompletableFuture<InvoiceInfoDTO>> futures = validInvoices.stream()
                    .map(invoice -> CompletableFuture.supplyAsync(() -> {
                        if (alreadyOverdueSet.contains(Long.valueOf(invoice.getMonthInvoiceId()))) {
                            return invoice;
                        }
                        try {
                            notificationService.sendOverdueNotification(
                                    invoice.getCustomerId(),
                                    invoice.getMonthInvoiceId(),
                                    invoice.getYearMonth(),
                                    invoice.getDigiCode(),
                                    invoice.getCustomerName(),
                                    invoice.getAddress()
                            );
                            sentCount.incrementAndGet();
                        } catch (Exception e) {
                            log.error("[OverdueNotify] Lỗi khi gửi thông báo quá hạn cho invoiceId={}: {}",
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
                log.error("[OverdueNotify] Timeout khi gửi thông báo quá hạn song song: {}", e.getMessage());
            }

            page++;
        }

        if (sentCount.get() > 0) {
            String msg = String.format("[OverdueNotify] Hoàn tất quét kỳ '%s': gửi mới=%d, bỏ qua=%d",
                    queryYm, sentCount.get(), skipCount.get());
            log.info(msg);
            businessLogger.info("TYPE: NOTIFICATION_SUMMARY | ACTION: OVERDUE_NOTIFY | STATUS: SUCCESS | MSG: {}", msg);
        } else {
            log.info("[OverdueNotify] Hoàn tất: không có thông báo quá hạn mới nào cần gửi.");
        }
    }

    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public List<InvoiceInfoDTO> fetchUnpaidInvoices(String yearMonth, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return monthInvoiceRepository.findUnpaidInvoiceDTOsByYearMonth(yearMonth, pageable);
    }

    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public Set<Long> fetchAlreadyOverdueIds(List<Long> invoiceIds) {
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            return Set.of();
        }
        List<Long> notifiedIds = notificationRepository.findOverdueInvoiceIdsIn(invoiceIds);
        return new java.util.HashSet<>(notifiedIds);
    }
}
