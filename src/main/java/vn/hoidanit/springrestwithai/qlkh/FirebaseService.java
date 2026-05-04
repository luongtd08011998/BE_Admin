package vn.hoidanit.springrestwithai.qlkh;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import vn.hoidanit.springrestwithai.feature.log.SystemLogService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wrapper gửi Push Notification qua Firebase Cloud Messaging (FCM).
 * Tất cả lỗi từ FCM đều được bắt và log để không ảnh hưởng luồng nghiệp vụ chính.
 *
 * <p>Sử dụng {@link #sendToMultipleTokensAsync} cho các tác vụ batch (Scheduler)
 * để không block thread chính. Method sync {@link #sendToMultipleTokens} vẫn
 * giữ cho các trường hợp cần gửi và đợi kết quả.
 */
@Service
public class FirebaseService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);
    private final SystemLogService systemLogService;

    public FirebaseService(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    /**
     * Gửi thông báo đến một device token cụ thể.
     */
    public void sendToToken(String deviceToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM sent to token={}: messageId={}", maskToken(deviceToken), response);
            systemLogService.logNotification(deviceToken, "SEND_FCM_TOKEN", "SUCCESS",
                    String.format("Sent to token: %s", title), response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM error for token={}: code={} message={}", maskToken(deviceToken),
                    e.getMessagingErrorCode(), e.getMessage());
            systemLogService.logNotification(deviceToken, "SEND_FCM_TOKEN", "FAILURE",
                    String.format("Error: %s", e.getMessage()), e.getMessagingErrorCode().toString());
        }
    }

    /**
     * Gửi thông báo đến nhiều device token cùng lúc (multicast, tối đa 500 token/lần).
     */
    public void sendToMultipleTokens(List<String> tokens, String title, String body) {
        if (tokens == null || tokens.isEmpty()) {
            log.debug("FCM multicast skipped — no tokens.");
            return;
        }
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            var result = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM multicast: successCount={} failureCount={}",
                    result.getSuccessCount(), result.getFailureCount());

            String status = result.getFailureCount() == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String description = String.format("Title: %s, Success: %d, Failure: %d",
                    title, result.getSuccessCount(), result.getFailureCount());
            systemLogService.logNotification("BATCH", "SEND_FCM_MULTICAST", status, description, null);

            if (result.getFailureCount() > 0) {
                result.getResponses().forEach(r -> {
                    if (!r.isSuccessful()) {
                        log.warn("FCM multicast partial failure: {}", r.getException().getMessage());
                    }
                });
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast error: {}", e.getMessage(), e);
            systemLogService.logNotification("BATCH", "SEND_FCM_MULTICAST", "FAILURE",
                    String.format("Error: %s", e.getMessage()), null);
        }
    }

    /**
     * Phiên bản bất đồng bộ — chạy trên thread pool {@code fcmTaskExecutor}.
     * Dùng cho Scheduler/batch job để không block thread chính.
     */
    @Async("fcmTaskExecutor")
    public void sendToMultipleTokensAsync(List<String> tokens, String title, String body) {
        sendToMultipleTokens(tokens, title, body);
    }

    /**
     * Gửi multicast kèm data payload (dùng cho FEEDBACK deep link).
     */
    public void sendToMultipleTokensWithData(List<String> tokens, String title, String body,
            java.util.Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            log.debug("FCM multicast with data skipped — no tokens.");
            return;
        }
        try {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());
            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }
            var result = FirebaseMessaging.getInstance().sendEachForMulticast(builder.build());
            log.info("FCM multicast with data: successCount={} failureCount={}",
                    result.getSuccessCount(), result.getFailureCount());

            String status = result.getFailureCount() == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String description = String.format("Title: %s (with data), Success: %d, Failure: %d",
                    title, result.getSuccessCount(), result.getFailureCount());
            systemLogService.logNotification("BATCH", "SEND_FCM_MULTICAST_DATA", status, description, null);

            if (result.getFailureCount() > 0) {
                result.getResponses().forEach(r -> {
                    if (!r.isSuccessful()) {
                        log.warn("FCM multicast partial failure: {}", r.getException().getMessage());
                    }
                });
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast with data error: {}", e.getMessage(), e);
            systemLogService.logNotification("BATCH", "SEND_FCM_MULTICAST_DATA", "FAILURE",
                    String.format("Error: %s", e.getMessage()), null);
        }
    }

    /** Phiên bản async của sendToMultipleTokensWithData. */
    @Async("fcmTaskExecutor")
    public void sendToMultipleTokensWithDataAsync(List<String> tokens, String title, String body,
            java.util.Map<String, String> data) {
        sendToMultipleTokensWithData(tokens, title, body, data);
    }

    /**
     * Gửi thông báo đến một topic cụ thể.
     */
    public void sendToTopic(String topic, String title, String body, java.util.Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("FCM sent to topic={}: messageId={}", topic, response);
            systemLogService.logNotification("TOPIC:" + topic, "SEND_FCM_TOPIC", "SUCCESS",
                    String.format("Title: %s", title), response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM error for topic={}: code={} message={}", topic,
                    e.getMessagingErrorCode(), e.getMessage());
            systemLogService.logNotification("TOPIC:" + topic, "SEND_FCM_TOPIC", "FAILURE",
                    String.format("Error: %s", e.getMessage()), e.getMessagingErrorCode().toString());
        }
    }

    /**
     * Phiên bản bất đồng bộ của sendToTopic.
     */
    @Async("fcmTaskExecutor")
    public void sendToTopicAsync(String topic, String title, String body, java.util.Map<String, String> data) {
        sendToTopic(topic, title, body, data);
    }

    /**
     * Unsubscribe tokens khỏi 1 topic (ví dụ: logout).
     */
    public void unsubscribeFromTopic(String topic, List<String> tokens) {
        if (topic == null || topic.isBlank()) {
            return;
        }
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        try {
            var res = FirebaseMessaging.getInstance().unsubscribeFromTopic(tokens, topic);
            log.info("FCM unsubscribe topic={} successCount={} failureCount={}",
                    topic, res.getSuccessCount(), res.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("FCM unsubscribe error for topic={}: code={} message={}",
                    topic, e.getMessagingErrorCode(), e.getMessage());
        }
    }

    @Async("fcmTaskExecutor")
    public void unsubscribeFromTopicAsync(String topic, List<String> tokens) {
        unsubscribeFromTopic(topic, tokens);
    }

    public void unsubscribeFromTopic(String topic, String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        unsubscribeFromTopic(topic, Collections.singletonList(token));
    }

    @Async("fcmTaskExecutor")
    public void unsubscribeFromTopicAsync(String topic, String token) {
        unsubscribeFromTopic(topic, token);
    }

    /** Ẩn một phần token để log an toàn hơn */
    private static String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
