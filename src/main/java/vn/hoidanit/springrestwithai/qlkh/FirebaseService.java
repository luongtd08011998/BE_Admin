package vn.hoidanit.springrestwithai.qlkh;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import vn.hoidanit.springrestwithai.feature.log.SystemLogService;
import vn.hoidanit.springrestwithai.qlkh.notification.dto.FCMBatchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Wrapper gửi Push Notification qua Firebase Cloud Messaging (FCM).
 * Mọi FCM call đều được bọc trong timeout (10s) để tránh block thread khi FCM server chậm.
 */
@Service
public class FirebaseService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseService.class);
    private static final long FCM_TIMEOUT_SECONDS = 10;

    private final SystemLogService systemLogService;
    private final Executor fcmTaskExecutor;

    public FirebaseService(SystemLogService systemLogService, @Qualifier("fcmTaskExecutor") Executor fcmTaskExecutor) {
        this.systemLogService = systemLogService;
        this.fcmTaskExecutor = fcmTaskExecutor;
    }

    public void sendToToken(String deviceToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
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

    public FCMBatchResult sendToMultipleTokens(List<String> tokens, String title, String body) {
        return sendToMultipleTokensWithData(tokens, title, body, null);
    }

    @Async("fcmTaskExecutor")
    public CompletableFuture<FCMBatchResult> sendToMultipleTokensAsync(List<String> tokens, String title, String body) {
        return CompletableFuture.completedFuture(sendToMultipleTokens(tokens, title, body));
    }

    public FCMBatchResult sendToMultipleTokensWithData(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            return FCMBatchResult.empty();
        }
        try {
            return CompletableFuture.supplyAsync(() -> doSendMulticast(tokens, title, body, data), fcmTaskExecutor)
                    .orTimeout(FCM_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        if (ex.getCause() instanceof TimeoutException || ex instanceof TimeoutException) {
                            log.error("FCM multicast TIMEOUT after {}s — tokens={}. Skipping.", FCM_TIMEOUT_SECONDS, tokens.size());
                        } else {
                            log.error("FCM multicast failed: {}", ex.getMessage());
                        }
                        return new FCMBatchResult(0, tokens.size(), List.of());
                    })
                    .join();
        } catch (Exception e) {
            log.error("FCM multicast error: {}", e.getMessage(), e);
            return new FCMBatchResult(0, tokens.size(), List.of());
        }
    }

    @Async("fcmTaskExecutor")
    public CompletableFuture<FCMBatchResult> sendToMultipleTokensWithDataAsync(List<String> tokens, String title, String body, Map<String, String> data) {
        return CompletableFuture.completedFuture(sendToMultipleTokensWithData(tokens, title, body, data));
    }

    private FCMBatchResult doSendMulticast(List<String> tokens, String title, String body, Map<String, String> data) {
        try {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }
            var result = FirebaseMessaging.getInstance().sendEachForMulticast(builder.build());
            log.info("FCM multicast: successCount={} failureCount={}", result.getSuccessCount(), result.getFailureCount());

            List<String> invalidTokens = new ArrayList<>();
            if (result.getFailureCount() > 0) {
                List<SendResponse> responses = result.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse r = responses.get(i);
                    if (!r.isSuccessful()) {
                        MessagingErrorCode code = r.getException().getMessagingErrorCode();
                        if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                            invalidTokens.add(tokens.get(i));
                        }
                    }
                }
            }

            String status = result.getFailureCount() == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String description = String.format("Title: %s, Success: %d, Failure: %d, InvalidTokens: %d",
                    title, result.getSuccessCount(), result.getFailureCount(), invalidTokens.size());
            systemLogService.logNotification("BATCH", "SEND_FCM_MULTICAST", status, description, null);

            return new FCMBatchResult(result.getSuccessCount(), result.getFailureCount(), invalidTokens);
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast error: {}", e.getMessage(), e);
            return new FCMBatchResult(0, tokens.size(), List.of());
        }
    }

    public FCMBatchResult sendToTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            return CompletableFuture.supplyAsync(() -> doSendTopic(topic, title, body, data), fcmTaskExecutor)
                    .orTimeout(FCM_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.error("FCM topic TIMEOUT or error for topic={}: {}", topic, ex.getMessage());
                        return new FCMBatchResult(0, 1, List.of());
                    })
                    .join();
        } catch (Exception e) {
            log.error("FCM topic error: {}", e.getMessage(), e);
            return new FCMBatchResult(0, 1, List.of());
        }
    }

    private FCMBatchResult doSendTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("FCM sent to topic={}: messageId={}", topic, response);
            systemLogService.logNotification("TOPIC:" + topic, "SEND_FCM_TOPIC", "SUCCESS",
                    String.format("Title: %s", title), response);
            return new FCMBatchResult(1, 0, List.of());
        } catch (FirebaseMessagingException e) {
            log.error("FCM error for topic={}: code={} message={}", topic, e.getMessagingErrorCode(), e.getMessage());
            return new FCMBatchResult(0, 1, List.of());
        }
    }

    @Async("fcmTaskExecutor")
    public CompletableFuture<FCMBatchResult> sendToTopicAsync(String topic, String title, String body, Map<String, String> data) {
        return CompletableFuture.completedFuture(sendToTopic(topic, title, body, data));
    }

    public void unsubscribeFromTopic(String topic, List<String> tokens) {
        if (topic == null || topic.isBlank() || tokens == null || tokens.isEmpty()) {
            return;
        }
        try {
            var res = FirebaseMessaging.getInstance().unsubscribeFromTopic(tokens, topic);
            log.info("FCM unsubscribe topic={} success={} failure={}", topic, res.getSuccessCount(), res.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("FCM unsubscribe error for topic={}: {}", topic, e.getMessage());
        }
    }

    @Async("fcmTaskExecutor")
    public void unsubscribeFromTopicAsync(String topic, List<String> tokens) {
        unsubscribeFromTopic(topic, tokens);
    }

    public void unsubscribeFromTopic(String topic, String token) {
        if (token == null || token.isBlank()) return;
        unsubscribeFromTopic(topic, Collections.singletonList(token));
    }

    @Async("fcmTaskExecutor")
    public void unsubscribeFromTopicAsync(String topic, String token) {
        unsubscribeFromTopic(topic, token);
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
