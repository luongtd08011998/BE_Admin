package vn.hoidanit.springrestwithai.qlkh.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.InvoiceSyncTrackerRepository;
import vn.hoidanit.springrestwithai.feature.notification.entity.InvoiceSyncTracker;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cron Job quét gia tăng hóa đơn mới và gửi Push Notification tự động.
 *
 * <p>Chạy mỗi 5 phút (cấu hình qua {@code app.invoice-notify.cron}).
 * Logic:
 * <ol>
 *   <li>Lấy mốc ID hóa đơn đã xử lý từ bảng {@code invoice_sync_tracker} theo ngày.</li>
 *   <li>Chỉ query hóa đơn có {@code monthInvoiceId > mốc} — tránh quét lại toàn bộ.</li>
 *   <li>Kiểm tra đã gửi thông báo chưa (bảng {@code notified_invoice}).</li>
 *   <li>Nếu chưa → gửi Push FCM + lưu dấu đã gửi.</li>
 *   <li>Cập nhật mốc ID mới nhất vào tracker (atomic update).</li>
 * </ol>
 */
@Component
public class InvoiceNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceNotificationScheduler.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_ACTIVITY");
    private static final int BATCH_SIZE = 100;

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final NotificationService notificationService;
    private final vn.hoidanit.springrestwithai.feature.notification.NotifiedInvoiceRepository notifiedInvoiceRepository;
    private final InvoiceSyncTrackerRepository invoiceSyncTrackerRepository;
    private final InvoiceNotificationScheduler self;

    public InvoiceNotificationScheduler(MonthInvoiceRepository monthInvoiceRepository,
                                        NotificationService notificationService,
                                        vn.hoidanit.springrestwithai.feature.notification.NotifiedInvoiceRepository notifiedInvoiceRepository,
                                        InvoiceSyncTrackerRepository invoiceSyncTrackerRepository,
                                        @Lazy InvoiceNotificationScheduler self) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.notificationService = notificationService;
        this.notifiedInvoiceRepository = notifiedInvoiceRepository;
        this.invoiceSyncTrackerRepository = invoiceSyncTrackerRepository;
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

        log.info("[InvoiceNotify] Bắt đầu quét gia tăng hóa đơn với datePrefix='{}'", queryDate);

        // 1. Khởi tạo tracker nếu chưa tồn tại
        self.ensureTracker(queryDate);

        // 2. Lấy mốc ID đã xử lý
        Integer lastProcessedId = self.getTrackerLastId(queryDate);
        if (lastProcessedId == null) {
            log.warn("[InvoiceNotify] Không tìm thấy tracker cho '{}'. Bỏ qua.", queryDate);
            return;
        }

        log.info("[InvoiceNotify] Mốc ID gia tăng hiện tại: {}", lastProcessedId);

        // 3. Quét gia tăng — chỉ lấy hóa đơn có ID > lastProcessedId
        AtomicInteger sentCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);
        int maxProcessedId = lastProcessedId;

        while (true) {
            List<InvoiceInfoDTO> invoices = self.fetchNewInvoices(queryDate, maxProcessedId, BATCH_SIZE);
            if (invoices.isEmpty()) {
                break;
            }

            log.info("[InvoiceNotify] Tìm thấy {} hóa đơn mới (sau ID {}).", invoices.size(), maxProcessedId);

            Set<Integer> notifiedSet = self.fetchAlreadyNotifiedIds(
                    invoices.stream().map(InvoiceInfoDTO::getMonthInvoiceId).toList());
            skipCount.addAndGet(notifiedSet.size());

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
                } catch (Exception e) {
                    log.error("[InvoiceNotify] Lỗi khi gửi thông báo cho invoiceId={}: {}",
                            invoice.getMonthInvoiceId(), e.getMessage(), e);
                }
            }

            // Cập nhật mốc tới ID lớn nhất trong batch
            maxProcessedId = invoices.get(invoices.size() - 1).getMonthInvoiceId();
        }

        // 4. Lưu mốc mới nhất (atomic update)
        if (maxProcessedId > lastProcessedId) {
            self.updateTracker(queryDate, maxProcessedId);
            log.info("[InvoiceNotify] Cập nhật mốc ID gia tăng mới: {}", maxProcessedId);
        }

        if (sentCount.get() > 0) {
            String msg = String.format("[InvoiceNotify] Hoàn tất quét gia tăng '%s': gửi mới=%d, bỏ qua=%d",
                    queryDate, sentCount.get(), skipCount.get());
            log.info(msg);
            businessLogger.info("TYPE: NOTIFICATION_SUMMARY | ACTION: INVOICE_NOTIFY | STATUS: SUCCESS | MSG: {}", msg);
        } else {
            log.info("[InvoiceNotify] Hoàn tất: không có thông báo mới nào.");
        }
    }

    @Transactional(value = "primaryTransactionManager")
    public void ensureTracker(String datePrefix) {
        if (invoiceSyncTrackerRepository.findByDatePrefix(datePrefix).isPresent()) {
            return;
        }
        invoiceSyncTrackerRepository.insertIfNotExists(datePrefix, 0);
        log.info("[InvoiceNotify] Đảm bảo tracker tồn tại cho datePrefix='{}'", datePrefix);
    }

    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public Integer getTrackerLastId(String datePrefix) {
        return invoiceSyncTrackerRepository.findByDatePrefix(datePrefix)
                .map(InvoiceSyncTracker::getLastProcessedInvoiceId)
                .orElse(null);
    }

    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public List<InvoiceInfoDTO> fetchNewInvoices(String datePrefix, Integer minId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return monthInvoiceRepository.findNewInvoicesByDatePrefix(datePrefix, minId, pageable);
    }

    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public Set<Integer> fetchAlreadyNotifiedIds(List<Integer> allIds) {
        List<Integer> notifiedIds = notifiedInvoiceRepository.findNotifiedInvoiceIds(allIds);
        return new java.util.HashSet<>(notifiedIds);
    }

    @Transactional(value = "primaryTransactionManager")
    public void updateTracker(String datePrefix, Integer newLastId) {
        int updated = invoiceSyncTrackerRepository.atomicUpdateLastProcessedId(datePrefix, newLastId);
        if (updated == 0) {
            log.warn("[InvoiceNotify] Tracker '{}' không được cập nhật.", datePrefix);
        }
    }
}
