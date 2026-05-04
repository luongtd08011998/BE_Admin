package vn.hoidanit.springrestwithai.feature.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_ACTIVITY");

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(SystemLog systemLog) {
        // 1. Ghi vào Database (Lưu trữ lâu dài)
        systemLogRepository.save(systemLog);

        // 2. Ghi vào File (Chia theo ngày trong logs/activity/)
        businessLogger.info("TYPE: {} | USER: {} | ACTION: {} | STATUS: {} | IP: {} | MSG: {}",
                systemLog.getLogType(),
                systemLog.getUserEmail() != null ? systemLog.getUserEmail() : "GUEST",
                systemLog.getAction(),
                systemLog.getStatus(),
                systemLog.getIpAddress() != null ? systemLog.getIpAddress() : "N/A",
                systemLog.getDescription());
    }

    public void logAdminAction(String email, String action, String status, String description, String metadata, String ipAddress) {
        SystemLog log = new SystemLog();
        log.setLogType(LogType.ADMIN_ACTION);
        log.setUserEmail(email);
        log.setAction(action);
        log.setStatus(status);
        log.setDescription(description);
        log.setMetadata(metadata);
        log.setIpAddress(ipAddress);
        this.log(log);
    }

    public void logNotification(String recipient, String action, String status, String description, String metadata) {
        SystemLog log = new SystemLog();
        log.setLogType(LogType.NOTIFICATION);
        log.setUserEmail(recipient);
        log.setAction(action);
        log.setStatus(status);
        log.setDescription(description);
        log.setMetadata(metadata);
        this.log(log);
    }
}
