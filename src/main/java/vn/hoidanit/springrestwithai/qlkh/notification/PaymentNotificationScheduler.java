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
import vn.hoidanit.springrestwithai.feature.notification.NotifiedPaymentRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;

/**
 * Cron Job phát hiện hóa đơn chuyển sang THANH TOÁN và gửi Push Notification.
 *
 * <p>Chạy mỗi 1 phút (cấu hình qua {@code app.payment-notify.cron}).
 * Logic:
 * <ol>
 *   <li>Lấy tất cả hóa đơn có PaymentStatus = 2 (đã thanh toán) từ DB qlkh.</li>
 *   <li>So sánh với bảng {@code notified_payment} — loại những hóa đơn đã gửi thông báo rồi.</li>
 *   <li>Gửi Push FCM + lưu dấu đã gửi cho những hóa đơn mới.</li>
 * </ol>
 */
@Component
public class PaymentNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationScheduler.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_ACTIVITY");

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final NotificationService notificationService;
    private final NotifiedPaymentRepository notifiedPaymentRepository;
    private final PaymentNotificationScheduler self;

    public PaymentNotificationScheduler(MonthInvoiceRepository monthInvoiceRepository,
                                        NotificationService notificationService,
                                        NotifiedPaymentRepository notifiedPaymentRepository,
                                        @Lazy PaymentNotificationScheduler self) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.notificationService = notificationService;
        this.notifiedPaymentRepository = notifiedPaymentRepository;
        this.self = self;
    }

    /**
     * Cron Job tự động — chạy mỗi 1 phút (múi giờ Asia/Ho_Chi_Minh).
     */
    @Scheduled(cron = "${app.payment-notify.cron:0 */1 * * * *}", zone = "Asia/Ho_Chi_Minh")
    public void checkAndNotifyPaidInvoices() {
        // Quét hóa đơn trong 3 tháng gần nhất (tháng hiện tại + 2 tháng trước)
        String fromYearMonth = YearMonth.now()
                .minusMonths(2)
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        log.info("[PaymentNotify] Bắt đầu quét hóa đơn đã thanh toán từ tháng {}...", fromYearMonth);

        // Lấy danh sách ID đã gửi — transaction ngắn trên PRIMARY
        log.info("[PaymentNotify] Lấy danh sách ID đã gửi...");
        List<Integer> alreadyNotifiedIds = self.fetchAlreadyNotifiedPaymentIds(fromYearMonth);
        log.info("[PaymentNotify] Đã gửi trước: {} ID.", alreadyNotifiedIds.size());
        if (alreadyNotifiedIds.isEmpty()) {
            alreadyNotifiedIds = List.of(-1);
        }

        int pageNum = 0;
        int pageSize = 100;
        AtomicInteger sentCount = new AtomicInteger(0);

        while (true) {
            log.info("[PaymentNotify] Đọc page {} (size={})...", pageNum, pageSize);
            // Đọc 1 page từ QLKH — transaction ngắn, trả connection ngay
            List<InvoiceInfoDTO> paidInvoices = self.fetchPaidInvoicePage(fromYearMonth, pageNum, pageSize);
            if (paidInvoices == null || paidInvoices.isEmpty()) {
                if (pageNum == 0) {
                    log.info("[PaymentNotify] Không có hóa đơn mới cần gửi thông báo thanh toán — kết thúc.");
                }
                break;
            }

            log.info("[PaymentNotify] Page {} có {} hóa đơn.", pageNum, paidInvoices.size());

            // Filter đã gửi rồi bằng Java (O(1) HashSet lookup) — không dùng NOT IN
            java.util.Set<Integer> notifiedSet = new java.util.HashSet<>(alreadyNotifiedIds);
            int idx = 0;
            for (InvoiceInfoDTO invoice : paidInvoices) {
                if (notifiedSet.contains(invoice.getMonthInvoiceId())) {
                    continue;
                }
                try {
                    boolean sent = notificationService.sendAndMarkPaymentNotification(
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
                        log.info("[PaymentNotify] Đã gửi {}/{} hóa đơn trên page {}.", idx, paidInvoices.size(), pageNum);
                    }
                } catch (Exception e) {
                    log.error("[PaymentNotify] Lỗi khi gửi thông báo thanh toán invoiceId={}: {}",
                            invoice.getMonthInvoiceId(), e.getMessage(), e);
                }
            }
            pageNum++;
        }

        if (sentCount.get() > 0) {
            String msg = String.format("[PaymentNotify] Hoàn tất: đã gửi thông báo thanh toán mới=%d", sentCount.get());
            log.info(msg);
            businessLogger.info("TYPE: NOTIFICATION_SUMMARY | ACTION: PAYMENT_NOTIFY | STATUS: SUCCESS | MSG: {}", msg);
        } else {
            log.info("[PaymentNotify] Không có hóa đơn mới cần gửi.");
        }
    }

    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public List<Integer> fetchAlreadyNotifiedPaymentIds(String fromYearMonth) {
        return notifiedPaymentRepository.findNotifiedMonthInvoiceIdsSince(fromYearMonth);
    }

    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public List<InvoiceInfoDTO> fetchPaidInvoicePage(String fromYearMonth, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<InvoiceInfoDTO> invoicePage = monthInvoiceRepository
                .findPaidInvoiceInfo(fromYearMonth, pageable);
        return invoicePage.getContent();
    }
}
