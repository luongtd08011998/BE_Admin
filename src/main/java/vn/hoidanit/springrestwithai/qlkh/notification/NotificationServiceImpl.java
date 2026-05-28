package vn.hoidanit.springrestwithai.qlkh.notification;
import vn.hoidanit.springrestwithai.qlkh.FirebaseService;
import vn.hoidanit.springrestwithai.qlkh.TokenCleanupService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.CustomerDeviceRepository;
import vn.hoidanit.springrestwithai.feature.notification.NotificationRepository;
import vn.hoidanit.springrestwithai.feature.notification.NotifiedInvoiceRepository;
import vn.hoidanit.springrestwithai.feature.notification.NotifiedPaymentRepository;
import vn.hoidanit.springrestwithai.feature.notification.entity.CustomerDevice;
import vn.hoidanit.springrestwithai.feature.notification.entity.DeliveryStatus;
import vn.hoidanit.springrestwithai.feature.notification.entity.Notification;
import vn.hoidanit.springrestwithai.feature.notification.entity.NotifiedInvoice;
import vn.hoidanit.springrestwithai.feature.notification.entity.NotifiedPayment;
import vn.hoidanit.springrestwithai.feature.notification.entity.SystemNotification;
import vn.hoidanit.springrestwithai.feature.notification.entity.SystemNotificationRead;
import vn.hoidanit.springrestwithai.feature.notification.SystemNotificationRepository;
import vn.hoidanit.springrestwithai.feature.notification.SystemNotificationReadRepository;
import vn.hoidanit.springrestwithai.feature.article.ArticleRepository;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;
import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackStatus;
import vn.hoidanit.springrestwithai.qlkh.notification.dto.NotificationResponse;
import vn.hoidanit.springrestwithai.qlkh.notification.dto.FCMBatchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import vn.hoidanit.springrestwithai.feature.article.Article;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoice;
import vn.hoidanit.springrestwithai.qlkh.invoice.SalesInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;

/**
 * Xử lý nghiệp vụ thông báo:
 * <ul>
 *   <li>Lưu device token FCM</li>
 *   <li>Gửi push notification khi có hóa đơn mới hoặc thanh toán thành công</li>
 * </ul>
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.company-name:CNTOCTIEN}")
    private String companyName;

    @Value("${app.hotline:}")
    private String hotline;

    @Value("${app.water-cutoff.employee-name:}")
    private String defaultCutoffEmployeeName;

    @Value("${app.water-cutoff.employee-phone:}")
    private String defaultCutoffEmployeePhone;

    private final CustomerDeviceRepository customerDeviceRepository;
    private final NotificationRepository notificationRepository;
    private final NotifiedInvoiceRepository notifiedInvoiceRepository;
    private final NotifiedPaymentRepository notifiedPaymentRepository;
    private final SystemNotificationRepository systemNotificationRepository;
    private final SystemNotificationReadRepository systemNotificationReadRepository;
    private final FirebaseService firebaseService;
    private final TokenCleanupService tokenCleanupService;
    private final ArticleRepository articleRepository;
    private final MonthInvoiceRepository monthInvoiceRepository;
    private final NotificationServiceImpl self;

    public NotificationServiceImpl(CustomerDeviceRepository customerDeviceRepository,
                               NotificationRepository notificationRepository,
                               NotifiedInvoiceRepository notifiedInvoiceRepository,
                               NotifiedPaymentRepository notifiedPaymentRepository,
                               SystemNotificationRepository systemNotificationRepository,
                               SystemNotificationReadRepository systemNotificationReadRepository,
                               FirebaseService firebaseService,
                               TokenCleanupService tokenCleanupService,
                               ArticleRepository articleRepository,
                               MonthInvoiceRepository monthInvoiceRepository,
                               @Lazy NotificationServiceImpl self) {
        this.customerDeviceRepository = customerDeviceRepository;
        this.notificationRepository = notificationRepository;
        this.notifiedInvoiceRepository = notifiedInvoiceRepository;
        this.notifiedPaymentRepository = notifiedPaymentRepository;
        this.systemNotificationRepository = systemNotificationRepository;
        this.systemNotificationReadRepository = systemNotificationReadRepository;
        this.firebaseService = firebaseService;
        this.tokenCleanupService = tokenCleanupService;
        this.articleRepository = articleRepository;
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.self = self;
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
    public void sendNewInvoiceNotification(Integer customerId, Integer monthInvoiceId) {
        String title = "Hóa đơn mới";
        String content = "Bạn có hóa đơn tháng mới. Vui lòng kiểm tra chi tiết trong ứng dụng.";
        saveAndPush(customerId, title, content, "INVOICE", Long.valueOf(monthInvoiceId));
    }

    /**
     * Gọi khi thanh toán thành công: lưu notification + push FCM.
     */
    @Transactional("primaryTransactionManager")
    public void sendPaymentSuccessNotification(Integer customerId, Integer monthInvoiceId) {
        String title = "[" + companyName + "] THÔNG BÁO XÁC NHẬN THANH TOÁN";
        String content = "Kính gửi Quý khách hàng,\n\n"
                + companyName + " trân trọng thông báo: Quý khách đã thanh toán hóa đơn thành công.\n\n"
                + "Chân thành cảm ơn Quý khách đã tin tưởng và sử dụng dịch vụ của chúng tôi.\n\n"
                + "Mọi thắc mắc cần hỗ trợ, Quý khách vui lòng liên hệ:\n\n"
                + "Trung tâm CSKH " + companyName
                + (hotline != null && !hotline.isBlank() ? "\n\nHotline: " + hotline : "");
        saveAndPush(customerId, title, content, "PAYMENT", Long.valueOf(monthInvoiceId));
    }

    /**
     * Gọi khi Admin gửi nhắc nợ: lưu notification + push FCM.
     */
    @Transactional("primaryTransactionManager")
    public void sendDebtReminderNotification(Integer customerId, Integer monthInvoiceId, String yearMonth, String digiCode, String customerName, Double amount, String address) {
        String formattedYm = formatYearMonth(yearMonth);
        java.text.NumberFormat currencyFormatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(amount != null ? amount : 0);
        String displayAddress = (address != null && !address.isBlank()) ? address : "";

        String title = "[" + companyName + "] THÔNG BÁO TIỀN NƯỚC KỲ " + formattedYm;
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(companyName).append(" trân trọng thông báo về việc tiền nước kỳ ")
                .append(formattedYm).append(". ");
        contentBuilder.append("Kính gửi khách hàng: ").append(customerName)
                .append(", Mã KH: ").append(digiCode);
        if (!displayAddress.isEmpty()) {
            contentBuilder.append(", địa chỉ: ").append(displayAddress);
        }
        contentBuilder.append(". Số tiền cần thanh toán là ").append(formattedAmount).append(" đồng. ");
        contentBuilder.append("Quý khách vui lòng thanh toán trong vòng 07 ngày kể từ khi nhận được thông báo này ")
                .append("qua các ứng dụng trực tuyến của ngân hàng, ví điện tử: Payoo, ZaloPay, VNPT Money, AirPay, MoMo ")
                .append("hoặc các điểm thu hộ: Circle K, Family Mart, Thế giới di động, Điện máy xanh, FPT và các bưu điện gần nhất. ");
        contentBuilder.append("Chân thành cảm ơn sự hợp tác của Quý khách. ");
        contentBuilder.append("Mọi chi tiết xin liên hệ Trung tâm CSKH ").append(companyName);
        if (hotline != null && !hotline.isBlank()) {
            contentBuilder.append(": ").append(hotline);
        }
        contentBuilder.append(".");
        String content = contentBuilder.toString();
        saveAndPush(customerId, title, content, "DEBT_REMINDER", Long.valueOf(monthInvoiceId));
    }

    /**
     * Gọi khi Admin gửi thông báo quá hạn: lưu notification + push FCM.
     */
    @Transactional("primaryTransactionManager")
    public void sendOverdueNotification(Integer customerId, Integer monthInvoiceId, String yearMonth, String digiCode, String customerName, String address) {
        String formattedYm = formatYearMonth(yearMonth);
        String displayAddress = (address != null && !address.isBlank()) ? address : "";

        String title = "[" + companyName + "] Thông báo về việc quá hạn thanh toán tiền nước";
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(companyName).append(" Thông báo về việc quá hạn thanh toán tiền nước. ");
        contentBuilder.append("Kính gửi khách hàng: ").append(customerName)
                .append(", Mã KH: ").append(digiCode);
        if (!displayAddress.isEmpty()) {
            contentBuilder.append(", địa chỉ: ").append(displayAddress);
        }
        contentBuilder.append(". Hóa đơn kỳ ").append(formattedYm)
                .append(" của Quý khách đã quá hạn thanh toán. ")
                .append("Quá thời hạn trên, chúng tôi xin phép được tạm ngưng cấp nước. ")
                .append("Việc cấp nước trở lại sẽ được thực hiện sau 24h kể từ khi Quý khách hoàn tất thanh toán tiền nước và chi phí mở nước 100.000 VND. ");
        contentBuilder.append("Trân trọng cảm ơn. ");
        contentBuilder.append("Mọi chi tiết xin liên hệ Trung tâm CSKH ").append(companyName);
        if (hotline != null && !hotline.isBlank()) {
            contentBuilder.append(": ").append(hotline);
        }
        contentBuilder.append(".");
        String content = contentBuilder.toString();
        saveAndPush(customerId, title, content, "OVERDUE", Long.valueOf(monthInvoiceId));
    }

    /**
     * Gọi khi Admin gửi thông báo cúp nước: lưu notification + push FCM.
     * Nếu employeeName/employeePhone null → dùng giá trị mặc định từ config.
     */
    @Transactional("primaryTransactionManager")
    public void sendWaterCutoffNotification(Integer customerId, Integer monthInvoiceId, String digiCode, String employeeName, String employeePhone) {
        String empName = (employeeName != null && !employeeName.isBlank()) ? employeeName : defaultCutoffEmployeeName;
        String empPhone = (employeePhone != null && !employeePhone.isBlank()) ? employeePhone : defaultCutoffEmployeePhone;

        String title = "[" + companyName + "] Thông báo cúp nước";
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("[").append(companyName).append("]: ");
        contentBuilder.append("Do quý khách quá hạn thanh toán tiền nước mã KH ").append(digiCode);
        contentBuilder.append(", việc cúp nước đã được chuyển cho nhân viên ").append(empName);
        contentBuilder.append(" - ").append(empPhone).append(" thực hiện. ");
        contentBuilder.append("Trân trọng. ");
        contentBuilder.append("Mọi chi tiết xin liên hệ Trung tâm CSKH ").append(companyName);
        if (hotline != null && !hotline.isBlank()) {
            contentBuilder.append(": ").append(hotline);
        }
        contentBuilder.append(".");
        String content = contentBuilder.toString();
        saveAndPush(customerId, title, content, "WATER_CUTOFF", Long.valueOf(monthInvoiceId));
    }

    /**
     * Gọi khi Admin đổi trạng thái phản ánh — thông báo đến KH ngay lập tức.
     */
    @Transactional("primaryTransactionManager")
    public void notifyFeedbackStatusChanged(Feedback feedback, FeedbackStatus newStatus) {
        String statusLabel = statusLabel(newStatus);
        String title = "Phản ánh của bạn đã được cập nhật";
        String content = feedback.getTrackingCode() + ": " + statusLabel;
        saveAndPush(feedback.getCustomerId(), title, content, "FEEDBACK", feedback.getId());
        log.info("Sent FEEDBACK status notification to customerId={} trackingCode={} newStatus={}",
                feedback.getCustomerId(), feedback.getTrackingCode(), newStatus);
    }

    /**
     * Gọi khi Admin gửi reply — thông báo đến KH có phản hồi mới.
     */
    @Transactional("primaryTransactionManager")
    public void notifyFeedbackReply(Feedback feedback, String replyContent) {
        String title = "Bạn có phản hồi mới từ nhân viên";
        String preview = replyContent != null && replyContent.length() > 80
                ? replyContent.substring(0, 80) + "..."
                : replyContent;
        String content = feedback.getTrackingCode() + ": " + preview;
        saveAndPush(feedback.getCustomerId(), title, content, "FEEDBACK", feedback.getId());
        log.info("Sent FEEDBACK reply notification to customerId={} trackingCode={}",
                feedback.getCustomerId(), feedback.getTrackingCode());
    }

    // ─── Notification CRUD ──────────────────────────────────────────────────

    public List<NotificationResponse> getNotifications(Integer customerId, String type, String excludeType) {
        // 1. Lấy thông báo cá nhân
        List<Notification> personalNotifs;
        if (type != null && !type.isBlank()) {
            personalNotifs = notificationRepository.findByCustomerIdAndTypeOrderByCreatedAtDesc(customerId, type);
        } else if (excludeType != null && !excludeType.isBlank()) {
            personalNotifs = notificationRepository.findByCustomerIdAndTypeNotOrderByCreatedAtDesc(customerId, excludeType);
        } else {
            personalNotifs = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        }

        List<NotificationResponse> responses = new ArrayList<>(personalNotifs.stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getCustomerId(),
                        n.getTitle(),
                        n.getContent(),
                        n.getType(),
                        n.getIsRead(),
                        n.getCreatedAt(),
                        n.getReferenceId(),
                        false,
                        null))
                .toList());

        // 2. Lấy thông báo hệ thống
        if (type == null || type.isBlank() || excludeType != null) {
            List<SystemNotification> systemNotifs = systemNotificationRepository.findAll();
        List<SystemNotificationRead> systemReads = systemNotificationReadRepository.findByCustomerId(customerId);
        Set<Long> readSystemIds = systemReads.stream()
                .map(SystemNotificationRead::getSystemNotificationId)
                .collect(Collectors.toSet());

        // Batch load articles — tránh N+1 query
        List<Long> articleIds = systemNotifs.stream()
                .map(SystemNotification::getReferenceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> articleSlugMap = articleIds.isEmpty()
                ? Map.of()
                : articleRepository.findAllById(articleIds).stream()
                        .collect(Collectors.toMap(Article::getId, Article::getSlug, (a, b) -> a));

        List<NotificationResponse> systemResponses = systemNotifs.stream()
                .filter(sn -> sn.getReferenceId() == null || articleSlugMap.containsKey(sn.getReferenceId()))
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
                        buildUrlFromMap(sn.getReferenceId(), articleSlugMap)
                )).toList();

            responses.addAll(systemResponses);
        }

        // 3. Sort tất cả theo thời gian giảm dần
        responses.sort((n1, n2) -> n2.createdAt().compareTo(n1.createdAt()));
        return responses;
    }

    public long getUnreadCount(Integer customerId, String type, String excludeType) {
        if (type != null && !type.isBlank()) {
            return notificationRepository.countUnreadByCustomerIdAndType(customerId, type);
        } else if (excludeType != null && !excludeType.isBlank()) {
            return notificationRepository.countUnreadByCustomerIdAndTypeNot(customerId, excludeType);
        } else {
            return notificationRepository.countUnreadByCustomerId(customerId);
        }
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

    public List<Long> findAllRemindedInvoiceIds() {
        return notificationRepository.findAllRemindedInvoiceIds();
    }

    public List<Long> findAllOverdueInvoiceIds() {
        return notificationRepository.findAllOverdueInvoiceIds();
    }

    public List<Long> findAllCutwaterInvoiceIds() {
        return notificationRepository.findAllCutwaterInvoiceIds();
    }

    public List<Integer> findAllInvoiceNotifiedIds() {
        return notifiedInvoiceRepository.findAllNotifiedMonthInvoiceIds();
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

        // Luôn gửi kèm data payload để Mobile deep link
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", type);
        if (referenceId != null) {
            data.put("referenceId", referenceId.toString());
        }

        firebaseService.sendToTopicAsync("general_news", title, content, data)
                .thenAccept(result -> {
                    if (result != null && !result.invalidTokens().isEmpty()) {
                        tokenCleanupService.cleanupInvalidTokens(result.invalidTokens());
                    }
                });
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
    public boolean sendAndMarkInvoiceNotification(Integer monthInvoiceId, Integer customerId, String yearMonth, String digiCode, String customerName, Double amount, String address) {
        // Kiểm tra đã gửi thông báo cho hóa đơn này chưa
        if (notifiedInvoiceRepository.existsByMonthInvoiceId(monthInvoiceId)) {
            log.debug("Invoice {} already notified — skipping.", monthInvoiceId);
            return false;
        }

        // Format số tiền theo chuẩn VND
        java.text.NumberFormat currencyFormatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(amount != null ? amount : 0);
        String formattedYm = formatYearMonth(yearMonth);
        String displayAddress = (address != null && !address.isBlank()) ? address : "";

        String title = "[" + companyName + "] THÔNG BÁO TIỀN NƯỚC KỲ " + formattedYm;
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Kính gửi Quý khách hàng ").append(customerName).append(",\n\n");
        contentBuilder.append(companyName).append(" xin thông báo hóa đơn tiền nước kỳ ")
                .append(formattedYm).append(" của Quý khách (Mã KH: ").append(digiCode);
        if (!displayAddress.isEmpty()) {
            contentBuilder.append(" - Địa chỉ: ").append(displayAddress);
        }
        contentBuilder.append(") có tổng số tiền thanh toán là ").append(formattedAmount).append(" đồng.\n\n");
        contentBuilder.append("Hạn thanh toán trong vòng 07 ngày kể từ khi nhận được thông báo này.\n\n");
        contentBuilder.append("Quý khách có thể thanh toán qua các Ví điện tử Payoo, ZaloPay, VNPT Money, AirPay, MoMo, ")
                .append("ứng dụng Ngân hàng hoặc tại các điểm thu hộ: Circle K, Family Mart, Thế giới di động, ")
                .append("Điện máy xanh, FPT và các bưu điện gần nhất.\n\n");
        contentBuilder.append("Chân thành cảm ơn sự hợp tác của Quý khách.\n\n");
        contentBuilder.append("Mọi chi tiết xin liên hệ Trung tâm CSKH ").append(companyName);
        if (hotline != null && !hotline.isBlank()) {
            contentBuilder.append(": ").append(hotline);
        }
        String content = contentBuilder.toString();
        saveAndPush(customerId, title, content, "INVOICE", Long.valueOf(monthInvoiceId));

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
    public boolean sendAndMarkPaymentNotification(Integer monthInvoiceId, Integer customerId, String yearMonth, String digiCode, String customerName, Double amount, String address) {
        // Kiểm tra đã gửi thông báo thanh toán cho hóa đơn này chưa
        if (notifiedPaymentRepository.existsByMonthInvoiceId(monthInvoiceId)) {
            log.debug("Payment invoice {} already notified — skipping.", monthInvoiceId);
            return false;
        }

        // Format số tiền theo chuẩn VND
        java.text.NumberFormat currencyFormatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(amount != null ? amount : 0);
        String formattedYm = formatYearMonth(yearMonth);
        String displayAddress = (address != null && !address.isBlank()) ? address : "";

        String title = "[" + companyName + "] THÔNG BÁO XÁC NHẬN THANH TOÁN";
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Kính gửi Quý khách hàng,\n\n");
        contentBuilder.append(companyName).append(" trân trọng thông báo: Hóa đơn tiền nước kỳ ")
                .append(formattedYm).append(" của Quý khách (Mã KH: ").append(digiCode);
        if (!displayAddress.isEmpty()) {
            contentBuilder.append(" - Địa chỉ: ").append(displayAddress);
        }
        contentBuilder.append(") đã được thanh toán thành công với số tiền ").append(formattedAmount).append(" đồng.\n\n");
        contentBuilder.append("Chân thành cảm ơn Quý khách đã tin tưởng và sử dụng dịch vụ của chúng tôi.\n\n");
        contentBuilder.append("Mọi thắc mắc cần hỗ trợ, Quý khách vui lòng liên hệ:\n\n");
        contentBuilder.append("Trung tâm CSKH ").append(companyName);
        if (hotline != null && !hotline.isBlank()) {
            contentBuilder.append("\n\nHotline: ").append(hotline);
        }
        String content = contentBuilder.toString();
        saveAndPush(customerId, title, content, "PAYMENT", Long.valueOf(monthInvoiceId));

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

    /**
     * Xoá SystemNotification + SystemNotificationRead liên quan đến 1 article.
     * Được gọi tự động khi xoá bài viết.
     */
    @Transactional("primaryTransactionManager")
    public void deleteSystemNotificationByArticleId(Long articleId) {
        List<SystemNotification> related = systemNotificationRepository.findAll().stream()
                .filter(sn -> articleId.equals(sn.getReferenceId()))
                .toList();
        for (SystemNotification sn : related) {
            systemNotificationReadRepository.deleteBySystemNotificationId(sn.getId());
        }
        if (!related.isEmpty()) {
            systemNotificationRepository.deleteAll(related);
            log.info("Deleted {} SystemNotification for articleId={}", related.size(), articleId);
        }
    }

    /**
     * Xoá SystemNotification mồ côi (referenceId trỏ đến article đã bị xóa).
     * Đồng thời xoá luôn các bản ghi read tương ứng.
     *
     * @return số notification đã xoá
     */
    @Transactional("primaryTransactionManager")
    public int cleanupOrphanedSystemNotifications() {
        List<SystemNotification> all = systemNotificationRepository.findAll();
        if (all.isEmpty()) return 0;

        List<Long> articleIds = all.stream()
                .map(SystemNotification::getReferenceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (articleIds.isEmpty()) return 0;

        Map<Long, String> existingArticles = articleRepository.findAllById(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, Article::getSlug, (a, b) -> a));

        List<SystemNotification> orphans = all.stream()
                .filter(sn -> sn.getReferenceId() != null && !existingArticles.containsKey(sn.getReferenceId()))
                .toList();

        if (orphans.isEmpty()) {
            log.info("[Cleanup] Không có SystemNotification mồ côi.");
            return 0;
        }

        List<Long> orphanIds = orphans.stream().map(SystemNotification::getId).toList();

        // Xoá read records trước (để không vi phạm FK nếu có)
        for (Long id : orphanIds) {
            systemNotificationReadRepository.deleteBySystemNotificationId(id);
        }

        systemNotificationRepository.deleteAll(orphans);
        log.info("[Cleanup] Đã xoá {} SystemNotification mồ côi: {}", orphans.size(), orphanIds);
        return orphans.size();
    }

    private static String formatYearMonth(String ym) {
        if (ym == null || ym.length() != 6) return ym;
        // "202604" → "04/2026"
        return ym.substring(4) + "/" + ym.substring(0, 4);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────────

    private String buildUrlFromMap(Long referenceId, Map<Long, String> articleSlugMap) {
        if (referenceId == null) return null;
        String slug = articleSlugMap.get(referenceId);
        return slug != null ? appBaseUrl + "/" + slug : null;
    }

    /** Overload không có referenceId — dùng cho INVOICE / PAYMENT. */
    private void saveAndPush(Integer customerId, String title, String content, String type, Long referenceId) {
        // 1. Lưu vào DB
        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        if (referenceId != null) {
            notification.setReferenceId(referenceId);
        }
        notificationRepository.save(notification);
        Long notificationId = notification.getId();

        // 2. Gửi FCM đến tất cả devices
        List<String> tokens = customerDeviceRepository.findByCustomerId(customerId)
                .stream()
                .map(CustomerDevice::getDeviceToken)
                .toList();

        if (tokens.isEmpty()) {
            log.debug("No devices registered for customerId={}. Notification saved but no push sent.", customerId);
            self.updateDeliveryStatus(notificationId, DeliveryStatus.NO_DEVICE, null);
            return;
        }

        // 3. Luôn gửi kèm data payload để Mobile deep link
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("type", type);
        if (referenceId != null) {
            data.put("referenceId", referenceId.toString());
        }

        try {
            firebaseService.sendToMultipleTokensWithDataAsync(tokens, title, content, data)
                    .thenAccept(result -> {
                        if (result != null) {
                            DeliveryStatus status;
                            if (result.successCount() > 0 && result.failureCount() == 0) {
                                status = DeliveryStatus.DELIVERED;
                            } else if (result.successCount() > 0) {
                                status = DeliveryStatus.PARTIAL;
                            } else {
                                status = DeliveryStatus.FAILED;
                            }
                            self.updateDeliveryStatus(notificationId, status, null);
                            if (!result.invalidTokens().isEmpty()) {
                                tokenCleanupService.cleanupInvalidTokens(result.invalidTokens());
                            }
                        }
                    })
                    .exceptionally(ex -> {
                        self.updateDeliveryStatus(notificationId, DeliveryStatus.FAILED, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("Failed to send FCM push to customerId={} (tokens: {}): {}",
                    customerId, tokens.size(), e.getMessage());
            notification.setDeliveryStatus(DeliveryStatus.FAILED);
            notification.setFailureReason(e.getMessage());
        }
    }

    @Transactional(value = "primaryTransactionManager")
    public void updateDeliveryStatus(Long notificationId, DeliveryStatus status, String failureReason) {
        try {
            notificationRepository.updateDeliveryStatus(notificationId, status,
                    status == DeliveryStatus.DELIVERED || status == DeliveryStatus.PARTIAL
                            ? java.time.LocalDateTime.now() : null,
                    failureReason);
        } catch (Exception e) {
            log.warn("Could not update delivery status for notificationId={}: {}", notificationId, e.getMessage());
        }
    }

    // ─── Backfill referenceId for old notifications ────────────────────────────

    /**
     * Backfill + sửa sai referenceId cho notification INVOICE/PAYMENT.
     * Fix cả 2 trường hợp:
     * <ul>
     *   <li>referenceId = NULL</li>
     *   <li>referenceId trỏ sai invoice (content_month != invoice_month)</li>
     * </ul>
     *
     * @return số notification đã được cập nhật
     */
    @Transactional("primaryTransactionManager")
    public int backfillNotificationReferenceId() {
        List<Notification> all = notificationRepository.findByTypeIn(List.of("INVOICE", "PAYMENT"));
        if (all.isEmpty()) {
            log.info("[Backfill] Không có notification INVOICE/PAYMENT nào.");
            return 0;
        }

        log.info("[Backfill] Quét {} notification INVOICE/PAYMENT...", all.size());

        // Pre-compute contentMonth
        Map<Long, String> notificationMonthMap = new HashMap<>();
        for (Notification n : all) {
            String contentMonth = extractYearMonthFromContent(n.getContent());
            if (contentMonth == null) {
                contentMonth = formatYearMonthFromLocalDateTime(n.getCreatedAt());
            }
            if (contentMonth != null) {
                notificationMonthMap.put(n.getId(), contentMonth);
            }
        }

        // Bước 1: Batch validate referenceId hiện tại (1 query duy nhất)
        List<Integer> existingRefIds = all.stream()
                .filter(n -> n.getReferenceId() != null)
                .map(n -> n.getReferenceId().intValue())
                .distinct()
                .toList();
        Map<Integer, MonthInvoice> refInvoiceMap = existingRefIds.isEmpty()
                ? Map.of()
                : monthInvoiceRepository.findAllById(existingRefIds).stream()
                        .collect(Collectors.toMap(MonthInvoice::getMonthInvoiceId, inv -> inv, (a, b) -> a));

        // Bước 2: Lọc ra CHỈ những notification thực sự cần sửa
        List<Notification> needsFix = new ArrayList<>();
        for (Notification n : all) {
            String contentMonth = notificationMonthMap.get(n.getId());
            if (contentMonth == null) continue;

            if (n.getReferenceId() == null) {
                needsFix.add(n);
                continue;
            }
            MonthInvoice currentInvoice = refInvoiceMap.get(n.getReferenceId().intValue());
            if (currentInvoice == null) {
                needsFix.add(n);
                continue;
            }
            boolean monthMatch = contentMonth.equals(currentInvoice.getYearMonth());
            boolean statusMatch = !"PAYMENT".equals(n.getType())
                    || Integer.valueOf(2).equals(currentInvoice.getPaymentStatus());
            if (!monthMatch || !statusMatch) {
                needsFix.add(n);
            }
        }

        if (needsFix.isEmpty()) {
            log.info("[Backfill] Tất cả {} notification đã đúng — không cần sửa.", all.size());
            return 0;
        }

        log.info("[Backfill] Cần sửa {}/{} notification.", needsFix.size(), all.size());

        // Bước 3: CHỈ load invoices cho notification cần sửa
        Map<String, List<MonthInvoice>> invoiceCache = new HashMap<>();
        Set<String> combos = new HashSet<>();
        for (Notification n : needsFix) {
            String month = notificationMonthMap.get(n.getId());
            if (month != null && n.getCustomerId() != null) {
                combos.add(n.getCustomerId() + "_" + month);
            }
        }
        for (String combo : combos) {
            String[] parts = combo.split("_");
            Integer custId = Integer.parseInt(parts[0]);
            String ym = parts[1];
            invoiceCache.put(combo, monthInvoiceRepository.findByCustomerIdAndYearMonth(custId, ym));
        }

        // Bước 4: Process
        int fixed = 0;
        List<Notification> toSave = new ArrayList<>();
        List<Notification> toDelete = new ArrayList<>();

        for (Notification n : needsFix) {
            try {
                String contentMonth = notificationMonthMap.get(n.getId());
                if (contentMonth == null) continue;

                String key = n.getCustomerId() + "_" + contentMonth;
                List<MonthInvoice> invoices = invoiceCache.get(key);
                if ("PAYMENT".equals(n.getType())) {
                    invoices = invoices != null
                            ? invoices.stream()
                                    .filter(inv -> Integer.valueOf(2).equals(inv.getPaymentStatus()))
                                    .toList()
                            : List.of();
                }

                if (invoices != null && !invoices.isEmpty()) {
                    n.setReferenceId(Long.valueOf(invoices.get(0).getMonthInvoiceId()));
                    toSave.add(n);
                    fixed++;
                    log.info("[Backfill] FIXED notificationId={} → monthInvoiceId={}",
                            n.getId(), invoices.get(0).getMonthInvoiceId());
                } else if ("PAYMENT".equals(n.getType())) {
                    toDelete.add(n);
                    fixed++;
                    log.warn("[Backfill] DELETED notificationId={} type=PAYMENT — không có hóa đơn đã thanh toán cho customerId={} month={}",
                            n.getId(), n.getCustomerId(), contentMonth);
                } else {
                    log.warn("[Backfill] notificationId={} customerId={} yearMonth={} không tìm thấy hóa đơn.",
                            n.getId(), n.getCustomerId(), contentMonth);
                }
            } catch (Exception e) {
                log.error("[Backfill] Lỗi notificationId={}: {}", n.getId(), e.getMessage());
            }
        }

        if (!toSave.isEmpty()) {
            notificationRepository.saveAll(toSave);
        }
        if (!toDelete.isEmpty()) {
            notificationRepository.deleteAll(toDelete);
        }

        log.info("[Backfill] Hoàn tất: sửa {}/{} notification.", fixed, all.size());
        return fixed;
    }

    /**
     * Parse "MM/yyyy" từ notification content → "yyyyMM".
     * VD: "Hóa đơn tiền nước tháng 03/2026 đã được thanh toán" → "202603"
     * Trả về null nếu content không chứa pattern.
     */
    private static String extractYearMonthFromContent(String content) {
        if (content == null) return null;
        var matcher = java.util.regex.Pattern.compile("(\\d{2})/(\\d{4})").matcher(content);
        if (matcher.find()) {
            return matcher.group(2) + matcher.group(1);
        }
        return null;
    }

    private static String formatYearMonthFromLocalDateTime(java.time.LocalDateTime ldt) {
        if (ldt == null) return null;
        return String.format("%d%02d", ldt.getYear(), ldt.getMonthValue());
    }

    private static String statusLabel(FeedbackStatus status) {
        return switch (status) {
            case PENDING    -> "Đang chờ xử lý";
            case PROCESSING -> "Đang xử lý";
            case RESOLVED   -> "Đã xử lý xong";
            case REJECTED   -> "Không tiếp nhận";
        };
    }
}
