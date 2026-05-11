package vn.hoidanit.springrestwithai.qlkh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.feature.notification.CustomerDeviceRepository;

import java.util.List;

/**
 * Service chuyên biệt để dọn dẹp các token FCM không hợp lệ.
 * Tách riêng ra để tránh lỗi tự gọi (self-invocation) khi xử lý giao dịch (Transaction)
 * trong các luồng chạy ngầm.
 */
@Service
public class TokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupService.class);
    private final CustomerDeviceRepository customerDeviceRepository;

    public TokenCleanupService(CustomerDeviceRepository customerDeviceRepository) {
        this.customerDeviceRepository = customerDeviceRepository;
    }

    /**
     * Thực hiện xóa các token không hợp lệ trong một giao dịch riêng biệt.
     */
    @Transactional("primaryTransactionManager")
    public void cleanupInvalidTokens(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        try {
            customerDeviceRepository.deleteByDeviceTokenIn(tokens);
            log.info("Cleaned up {} invalid device tokens in background thread", tokens.size());
        } catch (Exception e) {
            log.error("Failed to cleanup invalid tokens in background thread: {}", e.getMessage());
        }
    }
}
