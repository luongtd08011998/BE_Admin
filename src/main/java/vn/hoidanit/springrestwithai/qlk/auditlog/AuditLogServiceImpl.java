package vn.hoidanit.springrestwithai.qlk.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.hoidanit.springrestwithai.feature.user.User;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Async("auditLogExecutor")
    public void log(Long userId, String action, String entityName, Long entityId, String details, String ipAddress) {
        User user = null;
        if (userId != null) {
            user = new User();
            user.setId(userId);
        }

        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    public Page<AuditLog> findByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<AuditLog> findByEntity(String entityName, Long entityId, Pageable pageable) {
        if (entityId != null) {
            return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId, pageable);
        }
        return auditLogRepository.findByEntityName(entityName, pageable);
    }

    @Override
    public Page<AuditLog> findWithFilters(String entityName, String action, Long userId, Pageable pageable) {
        return auditLogRepository.findWithFilters(entityName, action, userId, pageable);
    }
}

