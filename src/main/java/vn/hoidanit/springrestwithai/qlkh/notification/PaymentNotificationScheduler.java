package vn.hoidanit.springrestwithai.qlkh.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.NotifiedPaymentRepository;
import vn.hoidanit.springrestwithai.feature.notification.PaymentSyncTrackerRepository;
import vn.hoidanit.springrestwithai.feature.notification.entity.PaymentSyncTracker;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO;
import vn.hoidanit.springrestwithai.qlkh.payment.PaymentLineRepository;
import vn.hoidanit.springrestwithai.qlkh.payment.entity.PaymentLine;

import java.util.List;

/**
 * Cron Job phát hiện thanh toán mới thông qua quét gia tăng bảng {@code paymentline}
 * và gửi Push Notification xác nhận thanh toán thành công về Mobile.
 *
 * <p>Chạy mỗi 1 phút (cấu hình qua {@code app.payment-notify.cron}).
 * Logic:
 * <ol>
 *   <li>Lấy mốc ID paymentline đã quét từ bảng {@code payment_sync_tracker} (DB Primary).</li>
 *   <li>Nếu chưa có mốc, tự động khởi tạo bằng ID lớn nhất hiện tại của {@code paymentline} (DB QLKH) làm mốc bắt đầu.</li>
 *   <li>Lấy danh sách các dòng thanh toán mới lớn hơn mốc này.</li>
 *   <li>Với mỗi thanh toán: Đối chiếu với danh sách hóa đơn cùng kỳ, loại trừ hóa đơn đã được thông báo trước đó để tránh gửi trùng, chọn hóa đơn phù hợp nhất, tiến hành gửi thông báo FCM.</li>
 *   <li>Cập nhật mốc ID mới nhất đã xử lý vào tracker.</li>
 * </ol>
 */
@Component
public class PaymentNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationScheduler.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_ACTIVITY");

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final NotificationService notificationService;
    private final NotifiedPaymentRepository notifiedPaymentRepository;
    private final PaymentLineRepository paymentLineRepository;
    private final PaymentSyncTrackerRepository paymentSyncTrackerRepository;
    private final PaymentNotificationScheduler self;

    public PaymentNotificationScheduler(MonthInvoiceRepository monthInvoiceRepository,
                                        NotificationService notificationService,
                                        NotifiedPaymentRepository notifiedPaymentRepository,
                                        PaymentLineRepository paymentLineRepository,
                                        PaymentSyncTrackerRepository paymentSyncTrackerRepository,
                                        @Lazy PaymentNotificationScheduler self) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.notificationService = notificationService;
        this.notifiedPaymentRepository = notifiedPaymentRepository;
        this.paymentLineRepository = paymentLineRepository;
        this.paymentSyncTrackerRepository = paymentSyncTrackerRepository;
        this.self = self;
    }

    /**
     * Cron Job tự động — chạy mỗi 1 phút (múi giờ Asia/Ho_Chi_Minh).
     */
    @Scheduled(cron = "${app.payment-notify.cron:0 */1 * * * *}", zone = "Asia/Ho_Chi_Minh")
    public void checkAndNotifyPaidInvoices() {
        log.info("[PaymentNotify] Bắt đầu quét hóa đơn đã thanh toán qua paymentline...");

        // 1. Khởi tạo tracker nếu chưa tồn tại, sau đó lấy mốc ID đã xử lý
        self.initializeTrackerIfNeeded();
        Integer lastProcessedId = self.getOrInitializeTracker();
        if (lastProcessedId == null) {
            log.warn("[PaymentNotify] Không thể xác định mốc quét gia tăng (lastProcessedId is null). Bỏ qua lượt quét này.");
            return;
        }

        log.info("[PaymentNotify] Mốc ID quét gia tăng hiện tại: {}", lastProcessedId);

        // 2. Lấy danh sách các dòng thanh toán mới phát sinh
        List<PaymentLine> newLines = self.fetchNewPaymentLines(lastProcessedId);
        if (newLines.isEmpty()) {
            log.info("[PaymentNotify] Không có dòng thanh toán mới nào.");
            return;
        }

        log.info("[PaymentNotify] Phát hiện {} dòng thanh toán mới.", newLines.size());

        int sentCount = 0;
        int maxProcessedId = lastProcessedId;

        // 3. Xử lý từng dòng thanh toán mới phát sinh
        for (PaymentLine pl : newLines) {
            try {
                if (pl.getCustomerId() == null || pl.getYearMonth() == null) {
                    log.warn("[PaymentNotify] Dòng thanh toán lineId={} thiếu customerId hoặc yearMonth — bỏ qua.", pl.getPaymentLineId());
                    maxProcessedId = pl.getPaymentLineId();
                    continue;
                }

                // Chuẩn hóa yearMonth của paymentline (lấy 6 chữ số đầu YYYYMM)
                String plYm = pl.getYearMonth().trim();
                if (plYm.length() > 6) {
                    plYm = plYm.substring(0, 6);
                }

                // Tìm các hóa đơn tương ứng với khách hàng và kỳ thanh toán này
                List<InvoiceInfoDTO> invoices = self.fetchInvoicesForCustomerAndPeriod(pl.getCustomerId(), plYm);
                if (invoices == null || invoices.isEmpty()) {
                    log.warn("[PaymentNotify] Không tìm thấy hóa đơn nào của khách hàng id={} trong kỳ {} tương ứng với paymentline lineId={}", 
                            pl.getCustomerId(), plYm, pl.getPaymentLineId());
                    maxProcessedId = pl.getPaymentLineId();
                    continue;
                }

                // Lọc ra các hóa đơn chưa được gửi thông báo trước đó để chống gửi trùng/nhận nhầm
                List<InvoiceInfoDTO> unnotifiedInvoices = self.filterUnnotifiedInvoices(invoices);
                if (unnotifiedInvoices.isEmpty()) {
                    log.debug("[PaymentNotify] Tất cả hóa đơn trong kỳ {} của khách hàng id={} đã được thông báo trước đó — bỏ qua.", plYm, pl.getCustomerId());
                    maxProcessedId = pl.getPaymentLineId();
                    continue;
                }

                // Chọn hóa đơn phù hợp nhất (khớp tiền nhất)
                InvoiceInfoDTO selectedInvoice = self.matchInvoice(unnotifiedInvoices, pl.getAmount());

                // Gửi thông báo xác nhận thanh toán thành công
                boolean sent = notificationService.sendAndMarkPaymentNotification(
                        selectedInvoice.getMonthInvoiceId(),
                        selectedInvoice.getCustomerId(),
                        selectedInvoice.getYearMonth(),
                        selectedInvoice.getDigiCode(),
                        selectedInvoice.getCustomerName(),
                        pl.getAmount(), // Dùng số tiền thanh toán thực tế của paymentline
                        selectedInvoice.getAddress()
                );

                if (sent) {
                    sentCount++;
                }

            } catch (Exception e) {
                log.error("[PaymentNotify] Lỗi khi xử lý dòng thanh toán lineId={}: {}", pl.getPaymentLineId(), e.getMessage(), e);
            }
            maxProcessedId = pl.getPaymentLineId();
        }

        // 4. Cập nhật mốc ID mới nhất đã quét
        if (maxProcessedId > lastProcessedId) {
            self.updateTracker(maxProcessedId);
            log.info("[PaymentNotify] Cập nhật mốc ID quét gia tăng mới: {}", maxProcessedId);
        }

        if (sentCount > 0) {
            String msg = String.format("[PaymentNotify] Hoàn tất: đã gửi %d thông báo thanh toán thành công mới.", sentCount);
            log.info(msg);
            businessLogger.info("TYPE: NOTIFICATION_SUMMARY | ACTION: PAYMENT_NOTIFY | STATUS: SUCCESS | MSG: {}", msg);
        } else {
            log.info("[PaymentNotify] Hoàn tất: không có thông báo mới nào được gửi.");
        }
    }

    /**
     * Lấy mốc ID đã quét hoặc khởi tạo mốc ban đầu (Bootstrap) bằng MAX(PaymentLineId) hiện tại.
     * Sử dụng read-only transaction để tránh optimistic locking.
     */
    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public Integer getOrInitializeTracker() {
        return paymentSyncTrackerRepository.findById(1L)
                .map(PaymentSyncTracker::getLastProcessedPaymentLineId)
                .orElse(null);
    }

    /**
     * Khởi tạo tracker nếu chưa tồn tại.
     * Dùng INSERT IGNORE để tránh cả race condition lẫn rollback-only exception.
     */
    @Transactional(value = "primaryTransactionManager")
    public void initializeTrackerIfNeeded() {
        if (paymentSyncTrackerRepository.findById(1L).isPresent()) {
            return;
        }
        Integer maxId = self.fetchMaxPaymentLineId();
        if (maxId == null) {
            maxId = 0;
        }
        paymentSyncTrackerRepository.insertIfNotExists(maxId);
        log.info("[PaymentNotify] Đảm bảo tracker tồn tại với mốc: {}", maxId);
    }

    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public Integer fetchMaxPaymentLineId() {
        return paymentLineRepository.findMaxPaymentLineId().orElse(null);
    }

    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public List<PaymentLine> fetchNewPaymentLines(Integer lastId) {
        return paymentLineRepository.findByPaymentLineIdGreaterThanOrderByPaymentLineIdAsc(lastId);
    }

    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public List<InvoiceInfoDTO> fetchInvoicesForCustomerAndPeriod(Integer customerId, String yearMonth) {
        return monthInvoiceRepository.findInvoiceInfoByCustomerAndYearMonth(customerId, yearMonth);
    }

    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public List<InvoiceInfoDTO> filterUnnotifiedInvoices(List<InvoiceInfoDTO> invoices) {
        List<Integer> ids = invoices.stream().map(InvoiceInfoDTO::getMonthInvoiceId).toList();
        List<Integer> notifiedIds = notifiedPaymentRepository.findNotifiedPaymentIds(ids);
        return invoices.stream()
                .filter(inv -> !notifiedIds.contains(inv.getMonthInvoiceId()))
                .toList();
    }

    /**
     * So khớp hóa đơn phù hợp nhất dựa theo số tiền.
     */
    public InvoiceInfoDTO matchInvoice(List<InvoiceInfoDTO> unnotifiedInvoices, Double paymentAmount) {
        if (unnotifiedInvoices.size() == 1) {
            return unnotifiedInvoices.get(0);
        }
        return unnotifiedInvoices.stream()
                .filter(inv -> {
                    double amt = inv.getAmount() != null ? inv.getAmount() : 0.0;
                    return Math.abs(amt - (paymentAmount != null ? paymentAmount : 0.0)) < 1.0;
                })
                .findFirst()
                .orElse(unnotifiedInvoices.get(0)); // Fallback lấy hóa đơn đầu tiên nếu không khớp tiền hoàn hảo
    }

    /**
     * Cập nhật mốc quét ID mới nhất bằng atomic update — tránh optimistic locking.
     */
    @Transactional(value = "primaryTransactionManager")
    public void updateTracker(Integer newLastId) {
        int updated = paymentSyncTrackerRepository.atomicUpdateLastProcessedId(newLastId);
        if (updated == 0) {
            log.warn("[PaymentNotify] Tracker không được cập nhật (id=1 không tồn tại hoặc newLastId <= current).");
        }
    }
}
