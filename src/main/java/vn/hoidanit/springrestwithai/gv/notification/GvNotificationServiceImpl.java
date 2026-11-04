package vn.hoidanit.springrestwithai.gv.notification;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.gv.notification.dto.GvNotificationResponse;
import vn.hoidanit.springrestwithai.gv.task.GvTask;
import vn.hoidanit.springrestwithai.gv.task.GvTaskRepository;
import vn.hoidanit.springrestwithai.qlkh.FirebaseService;

@Service
@Transactional
public class GvNotificationServiceImpl implements GvNotificationService {

    private static final Logger log = LoggerFactory.getLogger(GvNotificationServiceImpl.class);

    private final GvTaskNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final GvTaskRepository taskRepository;
    private final GvUserDeviceRepository userDeviceRepository;
    private final FirebaseService firebaseService;

    public GvNotificationServiceImpl(
            GvTaskNotificationRepository notificationRepository,
            UserRepository userRepository,
            GvTaskRepository taskRepository,
            GvUserDeviceRepository userDeviceRepository,
            FirebaseService firebaseService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userDeviceRepository = userDeviceRepository;
        this.firebaseService = firebaseService;
    }

    @Override
    @Async
    public void sendNotification(Long recipientId, Long taskId, GvNotificationType type, String title, String content) {
        if (recipientId == null) return;

        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) return;

        GvTask task = taskId != null ? taskRepository.findById(taskId).orElse(null) : null;

        GvTaskNotification noti = GvTaskNotification.builder()
                .recipient(recipient)
                .task(task)
                .type(type)
                .title(title)
                .content(content)
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        // Gửi push notification FCM tới thiết bị di động (nếu đã đăng ký token)
        try {
            List<GvUserDevice> devices = userDeviceRepository.findByUserId(recipientId);
            if (devices != null && !devices.isEmpty()) {
                List<String> tokens = devices.stream().map(GvUserDevice::getDeviceToken).toList();
                Map<String, String> data = new HashMap<>();
                data.put("taskId", taskId != null ? String.valueOf(taskId) : "");
                data.put("type", type != null ? type.name() : "");
                data.put("title", title != null ? title : "");
                data.put("content", content != null ? content : "");
                firebaseService.sendToMultipleTokensWithDataAsync(tokens, title, content, data);
            }
        } catch (Exception e) {
            log.warn("[GvNotification] Không thể gửi push FCM cho recipientId={}: {}", recipientId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GvNotificationResponse> getMyNotifications(Long recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Override
    public void markAsRead(Long notificationId, Long recipientId) {
        GvTaskNotification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", "id", notificationId));

        if (!noti.getRecipient().getId().equals(recipientId)) {
            throw new IllegalArgumentException("Bạn không có quyền đánh dấu thông báo này");
        }

        noti.setIsRead(true);
        noti.setReadAt(Instant.now());
        notificationRepository.save(noti);
    }

    @Override
    public void markAllAsRead(Long recipientId) {
        List<GvTaskNotification> unreadList = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId);
        Instant now = Instant.now();
        unreadList.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unreadList);
    }

    @Override
    public void registerDevice(Long userId, String deviceToken, String platform) {
        if (userId == null || deviceToken == null || deviceToken.isBlank()) return;
        userDeviceRepository.findByUserIdAndDeviceToken(userId, deviceToken).ifPresentOrElse(
                dev -> {
                    dev.setPlatform(platform != null ? platform : "ANDROID");
                    userDeviceRepository.save(dev);
                },
                () -> {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        GvUserDevice dev = GvUserDevice.builder()
                                .user(user)
                                .deviceToken(deviceToken)
                                .platform(platform != null ? platform : "ANDROID")
                                .build();
                        userDeviceRepository.save(dev);
                    }
                }
        );
    }

    @Override
    public void unregisterDevice(Long userId, String deviceToken) {
        if (userId == null || deviceToken == null || deviceToken.isBlank()) return;
        userDeviceRepository.deleteByUserIdAndDeviceToken(userId, deviceToken);
    }

    private GvNotificationResponse toResponse(GvTaskNotification noti) {
        return GvNotificationResponse.builder()
                .id(noti.getId())
                .taskId(noti.getTask() != null ? noti.getTask().getId() : null)
                .taskCode(noti.getTask() != null ? noti.getTask().getCode() : null)
                .taskTitle(noti.getTask() != null ? noti.getTask().getTitle() : null)
                .type(noti.getType())
                .title(noti.getTitle())
                .content(noti.getContent())
                .isRead(noti.getIsRead())
                .readAt(noti.getReadAt())
                .createdAt(noti.getCreatedAt())
                .build();
    }
}
