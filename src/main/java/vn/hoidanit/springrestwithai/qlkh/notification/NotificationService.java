package vn.hoidanit.springrestwithai.qlkh.notification;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;
import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackStatus;

import java.util.List;
import vn.hoidanit.springrestwithai.qlkh.notification.dto.NotificationResponse;



public interface NotificationService {
    void registerDevice(Integer customerId, String deviceToken, String platform);
    void unregisterDevice(Integer customerId, String deviceToken);
    void sendNewInvoiceNotification(Integer customerId, Integer monthInvoiceId);
    void sendPaymentSuccessNotification(Integer customerId, Integer monthInvoiceId);
    void sendDebtReminderNotification(Integer customerId, Integer monthInvoiceId, String yearMonth, String digiCode, String customerName, Double amount, String address);
    void sendOverdueNotification(Integer customerId, Integer monthInvoiceId, String yearMonth, String digiCode, String customerName, String address);
    void sendWaterCutoffNotification(Integer customerId, Integer monthInvoiceId, String digiCode, String employeeName, String employeePhone);
    void notifyFeedbackStatusChanged(Feedback feedback, FeedbackStatus newStatus);
    void notifyFeedbackReply(Feedback feedback, String replyContent);
    List<NotificationResponse> getNotifications(Integer customerId, String type, String excludeType);
    long getUnreadCount(Integer customerId, String type, String excludeType);
    int markAsRead(Integer customerId, List<Long> ids, Boolean isSystem);
    List<Long> findAllRemindedInvoiceIds();
    List<Long> findAllOverdueInvoiceIds();
    List<Long> findAllCutwaterInvoiceIds();
    void broadcastSystemNotification(String title, String content, String type, Long referenceId);
    boolean sendAndMarkInvoiceNotification(Integer monthInvoiceId, Integer customerId, String yearMonth, String digiCode, String customerName, Double amount, String address);
    boolean sendAndMarkPaymentNotification(Integer monthInvoiceId, Integer customerId, String yearMonth, String digiCode, String customerName, Double amount, String address);
    void deleteSystemNotificationByArticleId(Long articleId);
    int cleanupOrphanedSystemNotifications();
    int backfillNotificationReferenceId();
}
