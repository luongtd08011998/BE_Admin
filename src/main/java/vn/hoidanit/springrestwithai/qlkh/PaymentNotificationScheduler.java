package vn.hoidanit.springrestwithai.qlkh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.NotifiedPaymentRepository;
import vn.hoidanit.springrestwithai.qlkh.entity.MonthInvoice;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cron Job phát hiện hóa đơn chuyển sang THANH TOÁN và gửi Push Notification.
 *
 * <p>Chạy mỗi 2 phút (cấu hình qua {@code app.payment-notify.cron}).
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

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final NotificationService notificationService;
    private final NotifiedPaymentRepository notifiedPaymentRepository;

    public PaymentNotificationScheduler(MonthInvoiceRepository monthInvoiceRepository,
                                        NotificationService notificationService,
                                        NotifiedPaymentRepository notifiedPaymentRepository) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.notificationService = notificationService;
        this.notifiedPaymentRepository = notifiedPaymentRepository;
    }

    /**
     * Cron Job tự động — chạy mỗi 2 phút (múi giờ Asia/Ho_Chi_Minh).
     */
    @Scheduled(cron = "${app.payment-notify.cron:0 1/2 * * * *}", zone = "Asia/Ho_Chi_Minh")
    @Transactional("qlkhTransactionManager")
    public void checkAndNotifyPaidInvoices() {
        // Chỉ quét hóa đơn trong 3 tháng gần nhất (tháng hiện tại + 2 tháng trước)
        String fromYearMonth = YearMonth.now()
                .minusMonths(2)
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        log.debug("[PaymentNotify] Quét hóa đơn đã thanh toán từ tháng {} trở đi...", fromYearMonth);

        // ── Tối ưu: lấy toàn bộ ID đã gửi thông báo trước, loại trừ ngay ở SQL ──
        List<Integer> alreadyNotifiedIds = notifiedPaymentRepository
                .findNotifiedMonthInvoiceIdsSince(fromYearMonth);
        // Nếu list rỗng, JPA NOT IN sẽ lỗi → thêm sentinel value -1
        if (alreadyNotifiedIds.isEmpty()) {
            alreadyNotifiedIds = List.of(-1);
        }

        int pageNum = 0;
        int pageSize = 500; // Chunk size
        AtomicInteger sentCount = new AtomicInteger(0);

        while (true) {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            Page<MonthInvoice> invoicePage = monthInvoiceRepository
                    .findRecentPaidInvoicesExcluding(fromYearMonth, alreadyNotifiedIds, pageable);

            if (invoicePage.isEmpty()) {
                if (pageNum == 0) {
                    log.debug("[PaymentNotify] Không có hóa đơn mới cần gửi thông báo thanh toán — kết thúc.");
                }
                break;
            }

            List<MonthInvoice> paidInvoices = invoicePage.getContent();

            paidInvoices.forEach(invoice -> {
                try {
                    boolean sent = notificationService.sendAndMarkPaymentNotification(
                            invoice.getMonthInvoiceId(),
                            invoice.getCustomerId(),
                            invoice.getYearMonth()
                    );
                    if (sent) sentCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("[PaymentNotify] Lỗi khi gửi thông báo thanh toán invoiceId={}: {}",
                            invoice.getMonthInvoiceId(), e.getMessage(), e);
                }
            });

            pageNum++;
        }

        if (sentCount.get() > 0) {
            log.info("[PaymentNotify] Hoàn tất: đã gửi thông báo thanh toán mới={}",
                    sentCount.get());
        }
    }
}
