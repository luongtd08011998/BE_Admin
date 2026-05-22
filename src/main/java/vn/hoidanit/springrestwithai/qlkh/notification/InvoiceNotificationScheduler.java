package vn.hoidanit.springrestwithai.qlkh.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import vn.hoidanit.springrestwithai.qlkh.invoice.SalesInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;

/**
 * Cron Job quét hóa đơn mới và gửi Push Notification tự động.
 *
 * <p>Chạy mỗi 2 phút (cấu hình qua {@code app.invoice-notify.cron}).
 * Logic:
 * <ol>
 *   <li>Lấy danh sách hóa đơn được tạo trong ngày hôm nay từ DB qlkh.</li>
 *   <li>Với mỗi hóa đơn, kiểm tra đã gửi thông báo chưa (bảng {@code notified_invoice}).</li>
 *   <li>Nếu chưa → gửi Push FCM + lưu dấu đã gửi.</li>
 * </ol>
 */
@Component
public class InvoiceNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceNotificationScheduler.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_ACTIVITY");

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final NotificationService notificationService;
    private final vn.hoidanit.springrestwithai.feature.notification.NotifiedInvoiceRepository notifiedInvoiceRepository;

    public InvoiceNotificationScheduler(MonthInvoiceRepository monthInvoiceRepository,
                                        NotificationService notificationService,
                                        vn.hoidanit.springrestwithai.feature.notification.NotifiedInvoiceRepository notifiedInvoiceRepository) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.notificationService = notificationService;
        this.notifiedInvoiceRepository = notifiedInvoiceRepository;
    }

    /**
     * Cron Job tự động — chạy mỗi 2 phút (cấu hình qua app.invoice-notify.cron).
     */
    @Scheduled(cron = "${app.invoice-notify.cron:0 */2 * * * *}", zone = "Asia/Ho_Chi_Minh")
    public void checkAndNotifyNewInvoices() {
        checkAndNotifyNewInvoices(null);
    }

    /**
     * Kích hoạt thủ công với ngày tùy chỉnh.
     * @param datePrefix Prefix ngày để query (ví dụ "2026-04-23", "2026-04", "20260423").
     *                   Nếu null → dùng ngày hôm nay định dạng "yyyyMMdd".
     */
    @Transactional("qlkhTransactionManager")
    public void checkAndNotifyNewInvoices(String datePrefix) {
        String queryDate = (datePrefix != null && !datePrefix.isBlank())
                ? datePrefix.trim()
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        log.debug("[InvoiceNotify] Bắt đầu quét hóa đơn với datePrefix='{}'", queryDate);

        int pageNum = 0;
        int pageSize = 500;
        AtomicInteger sentCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);

        while (true) {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            Page<InvoiceInfoDTO> invoicePage = monthInvoiceRepository.findInvoiceInfoByCreatedDatePrefix(queryDate, pageable);

            if (invoicePage.isEmpty()) {
                if (pageNum == 0) {
                    log.debug("[InvoiceNotify] Không có hóa đơn nào với datePrefix='{}' — kết thúc.", queryDate);
                }
                break;
            }

            List<InvoiceInfoDTO> invoices = invoicePage.getContent();

            // Tối ưu N+1: Lấy danh sách tất cả ID hóa đơn trong cụm
            List<Integer> allIds = invoices.stream().map(InvoiceInfoDTO::getMonthInvoiceId).toList();

            // Lấy danh sách các ID đã được thông báo từ database primary
            List<Integer> notifiedIds = notifiedInvoiceRepository.findNotifiedInvoiceIds(allIds);
            java.util.Set<Integer> notifiedSet = new java.util.HashSet<>(notifiedIds);

            skipCount.addAndGet(notifiedSet.size());

            invoices.forEach(invoice -> {
                // Bỏ qua nếu đã gửi rồi
                if (notifiedSet.contains(invoice.getMonthInvoiceId())) {
                    return;
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
                }
            });

            pageNum++;
        }

        if (sentCount.get() > 0) {
            String msg = String.format("[InvoiceNotify] Hoàn tất quét '%s': đã gửi mới=%d, bỏ qua=%d",
                    queryDate, sentCount.get(), skipCount.get());
            log.info(msg);
            businessLogger.info("TYPE: NOTIFICATION_SUMMARY | ACTION: INVOICE_NOTIFY | STATUS: SUCCESS | MSG: {}", msg);
        }
    }
}
