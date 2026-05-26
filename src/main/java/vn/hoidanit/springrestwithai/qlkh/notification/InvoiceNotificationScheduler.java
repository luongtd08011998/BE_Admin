package vn.hoidanit.springrestwithai.qlkh.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;

/**
 * Cron Job quét hóa đơn mới và gửi Push Notification tự động.
 *
 * <p>Chạy mỗi 5 phút (cấu hình qua {@code app.invoice-notify.cron}).
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

    /**
     * Cron Job tự động — chạy mỗi 5 phút (cấu hình qua app.invoice-notify.cron).
     */
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

        int pageNum = 0;
        int pageSize = 100;
        AtomicInteger sentCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);

        while (true) {
            log.info("[InvoiceNotify] Đọc page {} (size={})...", pageNum, pageSize);
            // Đọc 1 page từ QLKH — transaction ngắn, trả connection ngay
            List<InvoiceInfoDTO> invoices = self.fetchInvoicePage(queryDate, pageNum, pageSize);
            if (invoices == null || invoices.isEmpty()) {
                if (pageNum == 0) {
                    log.info("[InvoiceNotify] Không có hóa đơn nào với datePrefix='{}' — kết thúc.", queryDate);
                }
                break;
            }

            log.info("[InvoiceNotify] Page {} có {} hóa đơn.", pageNum, invoices.size());

            // Kiểm tra đã gửi chưa — transaction ngắn trên PRIMARY
            List<Integer> allIds = invoices.stream().map(InvoiceInfoDTO::getMonthInvoiceId).toList();
            Set<Integer> notifiedSet = self.fetchAlreadyNotifiedIds(allIds);
            skipCount.addAndGet(notifiedSet.size());

            // Gửi notification — KHÔNG giữ transaction, mỗi sendAndMark có transaction riêng
            int idx = 0;
            for (InvoiceInfoDTO invoice : invoices) {
                if (notifiedSet.contains(invoice.getMonthInvoiceId())) {
                    continue;
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
                    idx++;
                    if (idx % 50 == 0) {
                        log.info("[InvoiceNotify] Đã gửi {}/{} hóa đơn trên page {}.", idx, invoices.size(), pageNum);
                    }
                } catch (Exception e) {
                    log.error("[InvoiceNotify] Lỗi khi gửi thông báo cho invoiceId={}: {}",
                            invoice.getMonthInvoiceId(), e.getMessage(), e);
                }
            }
            pageNum++;
        }

        if (sentCount.get() > 0) {
            String msg = String.format("[InvoiceNotify] Hoàn tất quét '%s': đã gửi mới=%d, bỏ qua=%d",
                    queryDate, sentCount.get(), skipCount.get());
            log.info(msg);
            businessLogger.info("TYPE: NOTIFICATION_SUMMARY | ACTION: INVOICE_NOTIFY | STATUS: SUCCESS | MSG: {}", msg);
        }
    }

    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public List<InvoiceInfoDTO> fetchInvoicePage(String queryDate, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<InvoiceInfoDTO> invoicePage = monthInvoiceRepository.findInvoiceInfoByCreatedDatePrefix(queryDate, pageable);
        return invoicePage.getContent();
    }

    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public Set<Integer> fetchAlreadyNotifiedIds(List<Integer> allIds) {
        List<Integer> notifiedIds = notifiedInvoiceRepository.findNotifiedInvoiceIds(allIds);
        return new java.util.HashSet<>(notifiedIds);
    }
}
