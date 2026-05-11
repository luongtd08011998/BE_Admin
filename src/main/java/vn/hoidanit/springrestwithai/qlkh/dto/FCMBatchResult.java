package vn.hoidanit.springrestwithai.qlkh.dto;

import java.util.List;

/**
 * Kết quả gửi thông báo batch qua FCM.
 */
public record FCMBatchResult(
        int successCount,
        int failureCount,
        List<String> invalidTokens
) {
    public static FCMBatchResult empty() {
        return new FCMBatchResult(0, 0, List.of());
    }
}
