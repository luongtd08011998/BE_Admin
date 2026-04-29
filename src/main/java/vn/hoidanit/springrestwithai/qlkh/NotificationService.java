package vn.hoidanit.springrestwithai.qlkh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.CustomerDeviceRepository;
import vn.hoidanit.springrestwithai.feature.notification.NotificationRepository;
import vn.hoidanit.springrestwithai.feature.notification.NotifiedInvoiceRepository;
import vn.hoidanit.springrestwithai.feature.notification.NotifiedPaymentRepository;
import vn.hoidanit.springrestwithai.feature.notification.entity.CustomerDevice;
import vn.hoidanit.springrestwithai.feature.notification.entity.Notification;
import vn.hoidanit.springrestwithai.feature.notification.entity.NotifiedInvoice;
import vn.hoidanit.springrestwithai.feature.notification.entity.NotifiedPayment;
import vn.hoidanit.springrestwithai.feature.notification.entity.SystemNotification;
import vn.hoidanit.springrestwithai.feature.notification.entity.SystemNotificationRead;
import vn.hoidanit.springrestwithai.feature.notification.SystemNotificationRepository;
import vn.hoidanit.springrestwithai.feature.notification.SystemNotificationReadRepository;
import vn.hoidanit.springrestwithai.feature.article.ArticleRepository;
import vn.hoidanit.springrestwithai.qlkh.dto.NotificationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Xử lý nghiệp vụ thông báo:
 * <ul>
 *   <li>Lưu device token FCM</li>
 *   <li>Gửi push notification khi có hóa đơn mới hoặc thanh toán thành công</li>
 * </ul>
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${app.base-url}")
    private String appBaseUrl;

    private final CustomerDeviceRepository customerDeviceRepository;
    private final NotificationRepository notificationRepository;
    private final NotifiedInvoiceRepository notifiedInvoiceRepository;
    private final NotifiedPaymentRepository notifiedPaymentRepository;
    private final SystemNotificationRepository systemNotificationRepository;
    private final SystemNotificationReadRepository systemNotificationReadRepository;
    private final FirebaseService firebaseService;
    private final ArticleRepository articleRepository;

    public NotificationService(CustomerDeviceRepository customerDeviceRepository,
                               NotificationRepository notificationRepository,
                               NotifiedInvoiceRepository notifiedInvoiceRepository,
                               NotifiedPaymentRepository notifiedPaymentRepository,
                               SystemNotificationRepository systemNotificationRepository,
                               SystemNotificationReadRepository systemNotificationReadRepository,
                               FirebaseService firebaseService,
                               ArticleRepository articleRepository) {
        this.customerDeviceRepository = customerDeviceRepository;
        this.notificationRepository = notificationRepository;
        this.notifiedInvoiceRepository = notifiedInvoiceRepository;
        this.notifiedPaymentRepository = notifiedPaymentRepository;
        this.systemNotificationRepository = systemNotificationRepository;
        this.systemNotificationReadRepository = systemNotificationReadRepository;
        this.firebaseService = firebaseService;
        this.articleRepository = articleRepository;
    }

    // ─── Device Token ───────────────────────────────────────────────────────

    /**
     * Lưu device token; bỏ qua nếu token đã tồn tại cho customer này.
     */
    @Transactional("primaryTransactionManager")
    public void registerDevice(Integer customerId, String deviceToken, String platform) {
        if (customerDeviceRepository.existsByCustomerIdAndDeviceToken(customerId, deviceToken)) {
            log.debug("Device token already registered for customerId={}", customerId);
            return;
        }
        CustomerDevice device = new CustomerDevice();
        device.setCustomerId(customerId);
        device.setDeviceToken(deviceToken);
        device.setPlatform(platform);
        customerDeviceRepository.save(device);
        log.info("Registered device for customerId={} platform={}", customerId, platform);
    }

    /**
     * Huỷ đăng ký 1 device token (thường dùng khi logout).
     * Idempotent: token không tồn tại thì coi như thành công.
     */
    @Transactional("primaryTransactionManager")
    public void unregisterDevice(Integer customerId, String deviceToken) {
        long deleted = customerDeviceRepository.deleteByCustomerIdAndDeviceToken(customerId, deviceToken);
        firebaseService.unsubscribeFromTopicAsync("general_news", deviceToken);
        log.info("Unregistered device token for customerId={} deleted={}", customerId, deleted);
    }

    // ─── Push Notification ───────────────────────────────────────────────────

    /**
     * Gọi khi có hóa đơn mới: lưu notification + push FCM đến mọi device của customer.
     */
    @Transactional("primaryTransactionManager")
    public void sendNewInvoiceNotification(Integer customerId) {
        String title = "Hóa đơn mới";
        String content = "Bạn có hóa đơn tháng mới. Vui lòng kiểm tra chi tiết trong ứng dụng.";
        saveAndPush(customerId, title, content, "INVOICE");
    }

    /**
     * Gọi khi thanh toán thành công: lưu notification + push FCM.
     */
    @Transactional("primaryTransactionManager")
    public void sendPaymentSuccessNotification(Integer customerId) {
        String title = "Thanh toán thành công";
        String content = "Bạn đã thanh toán hóa đơn thành công. Cảm ơn bạn!";
        saveAndPush(customerId, title, content, "PAYMENT");
    }

    // ─── Notification CRUD ──────────────────────────────────────────────────

    public List<NotificationResponse> getNotifications(Integer customerId) {
        // 1. Lấy thông báo cá nhân
        List<Notification> personalNotifs = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<NotificationResponse> responses = new ArrayList<>(personalNotifs.stream()
                .map(n -> new NotificationResponse(n.getId(), n.getCustomerId(), n.getTitle(), n.getContent(), n.getType(), n.getIsRead(), n.getCreatedAt(), null, false, null))
                .toList());

        // 2. Lấy thông báo hệ thống
        List<SystemNotification> systemNotifs = systemNotificationRepository.findAll();
        List<SystemNotificationRead> systemReads = systemNotificationReadRepository.findByCustomerId(customerId);
        List<Long> readSystemIds = systemReads.stream().map(SystemNotificationRead::getSystemNotificationId).toList();

        List<NotificationResponse> systemResponses = systemNotifs.stream()
                .map(sn -> new NotificationResponse(
                        sn.getId(),
                        customerId,
                        sn.getTitle(),
                        sn.getContent(),
                        sn.getType(),
                        readSystemIds.contains(sn.getId()),
                        sn.getCreatedAt(),
                        sn.getReferenceId(),
                        true,
                        buildUrl(sn.getReferenceId())
                )).toList();

        responses.addAll(systemResponses);

        // 3. Sort tất cả theo thời gian giảm dần
        responses.sort((n1, n2) -> n2.createdAt().compareTo(n1.createdAt()));
        return responses;
    }

    @Transactional("primaryTransactionManager")
    public int markAsRead(Integer customerId, List<Long> ids, Boolean isSystem) {
        if (isSystem != null && isSystem) {
            if (ids == null || ids.isEmpty()) {
                // Đánh dấu tất cả system notification là đã đọc
                List<SystemNotification> allSystemNotifs = systemNotificationRepository.findAll();
                for (SystemNotification sn : allSystemNotifs) {
                    if (!systemNotificationReadRepository.existsBySystemNotificationIdAndCustomerId(sn.getId(), customerId)) {
                        SystemNotificationRead read = new SystemNotificationRead();
                        read.setSystemNotificationId(sn.getId());
                        read.setCustomerId(customerId);
                        systemNotificationReadRepository.save(read);
                    }
                }
                return allSystemNotifs.size();
            } else {
                int count = 0;
                for (Long id : ids) {
                    if (!systemNotificationReadRepository.existsBySystemNotificationIdAndCustomerId(id, customerId)) {
                        SystemNotificationRead read = new SystemNotificationRead();
                        read.setSystemNotificationId(id);
                        read.setCustomerId(customerId);
                        systemNotificationReadRepository.save(read);
                        count++;
                    }
                }
                return count;
            }
        } else {
            if (ids == null || ids.isEmpty()) {
                return notificationRepository.markAllAsRead(customerId);
            }
            return notificationRepository.markAsRead(customerId, ids);
        }
    }

    /**
     * Gửi thông báo hệ thống (FCM Topic)
     */
    @Transactional("primaryTransactionManager")
    public void broadcastSystemNotification(String title, String content, String type, Long referenceId) {
        SystemNotification sn = new SystemNotification();
        sn.setTitle(title);
        sn.setContent(content);
        sn.setType(type);
        sn.setReferenceId(referenceId);
        systemNotificationRepository.save(sn);

        Map<String, String> data = null;
        if (referenceId != null) {
            data = Map.of("referenceId", referenceId.toString(), "type", type);
        }

        firebaseService.sendToTopicAsync("general_news", title, content, data);
        log.info("Broadcasted system notification: title={}, referenceId={}", title, referenceId);
    }

    // ─── Cron Job support ────────────────────────────────────────────────────

    /**
     * Gửi thông báo cho một hóa đơn cụ thể (được gọi bởi Cron Job).
     * Kiểm tra đã gửi chưa để tránh gửi trùng, sau đó lưu dấu đã gửi.
     *
     * @return true nếu đã gửi thành công, false nếu đã gửi trước đó (bỏ qua)
     */
    @Transactional("primaryTransactionManager")
    public boolean sendAndMarkInvoiceNotification(Integer monthInvoiceId, Integer customerId, String yearMonth) {
        // Kiểm tra đã gửi thông báo cho hóa đơn này chưa
        if (notifiedInvoiceRepository.existsByMonthInvoiceId(monthInvoiceId)) {
            log.debug("Invoice {} already notified — skipping.", monthInvoiceId);
            return false;
        }

        // Gửi push + lưu vào bảng notification
        String title = "Hóa đơn mới tháng " + formatYearMonth(yearMonth);
        String content = "Hóa đơn tiền nước tháng " + formatYearMonth(yearMonth)
                + " của bạn đã có. Vui lòng kiểm tra và thanh toán đúng hạn.";
        saveAndPush(customerId, title, content, "INVOICE");

        // Đánh dấu đã gửi
        NotifiedInvoice mark = new NotifiedInvoice();
        mark.setMonthInvoiceId(monthInvoiceId);
        mark.setCustomerId(customerId);
        mark.setYearMonth(yearMonth);
        notifiedInvoiceRepository.save(mark);

        log.info("Sent invoice notification: invoiceId={} customerId={} yearMonth={}",
                monthInvoiceId, customerId, yearMonth);
        return true;
    }

    /**
     * Gọi khi phát hiện hóa đơn chuyển sang trạng thái ĐÃ THANH TOÁN.
     * Kiểm tra đã gửi thông báo thanh toán chưa, nếu chưa thì gửi và đánh dấu.
     *
     * @return true nếu đã gửi thành công, false nếu đã gửi trước đó (bỏ qua)
     */
    @Transactional("primaryTransactionManager")
    public boolean sendAndMarkPaymentNotification(Integer monthInvoiceId, Integer customerId, String yearMonth) {
        // Kiểm tra đã gửi thông báo thanh toán cho hóa đơn này chưa
        if (notifiedPaymentRepository.existsByMonthInvoiceId(monthInvoiceId)) {
            log.debug("Payment invoice {} already notified — skipping.", monthInvoiceId);
            return false;
        }

        // Gửi push + lưu vào bảng notification
        String title = "Thanh toán thành công";
        String content = "Hóa đơn tiền nước tháng " + formatYearMonth(yearMonth)
                + " đã được thanh toán thành công. Cảm ơn bạn!";
        saveAndPush(customerId, title, content, "PAYMENT");

        // Đánh dấu đã gửi
        NotifiedPayment mark = new NotifiedPayment();
        mark.setMonthInvoiceId(monthInvoiceId);
        mark.setCustomerId(customerId);
        mark.setYearMonth(yearMonth);
        notifiedPaymentRepository.save(mark);

        log.info("Sent payment notification: invoiceId={} customerId={} yearMonth={}",
                monthInvoiceId, customerId, yearMonth);
        return true;
    }

    private static String formatYearMonth(String ym) {
        if (ym == null || ym.length() != 6) return ym;
        // "202604" → "04/2026"
        return ym.substring(4) + "/" + ym.substring(0, 4);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String buildUrl(Long referenceId) {
        if (referenceId == null) return null;
        return articleRepository.findById(referenceId)
                .map(article -> appBaseUrl + "/" + article.getSlug())
                .orElse(null);
    }

    private void saveAndPush(Integer customerId, String title, String content, String type) {
        // 1. Lưu vào DB
        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notificationRepository.save(notification);

        // 2. Gửi FCM đến tất cả devices
        List<String> tokens = customerDeviceRepository.findByCustomerId(customerId)
                .stream()
                .map(CustomerDevice::getDeviceToken)
                .toList();

        if (tokens.isEmpty()) {
            log.debug("No devices registered for customerId={}. Notification saved but no push sent.", customerId);
            return;
        }
        
        try {
            firebaseService.sendToMultipleTokensAsync(tokens, title, content);
        } catch (Exception e) {
            log.error("Failed to send FCM push to customerId={} (tokens: {}): {}", 
                    customerId, tokens.size(), e.getMessage());
            // KHÔNG throw lỗi ra ngoài để Transaction vẫn được commit,
            // đảm bảo cờ "đã gửi" được lưu, tránh kẹt cron job.
        }
    }
}
