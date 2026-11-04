package vn.hoidanit.springrestwithai.gv.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.gv.notification.dto.GvNotificationResponse;

public interface GvNotificationService {

    void sendNotification(Long recipientId, Long taskId, GvNotificationType type, String title, String content);

    Page<GvNotificationResponse> getMyNotifications(Long recipientId, Pageable pageable);

    long getUnreadCount(Long recipientId);

    void markAsRead(Long notificationId, Long recipientId);

    void markAllAsRead(Long recipientId);

    void registerDevice(Long userId, String deviceToken, String platform);

    void unregisterDevice(Long userId, String deviceToken);
}
