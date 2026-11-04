package vn.hoidanit.springrestwithai.gv.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.hoidanit.springrestwithai.feature.user.User;

@Service
public class GvAuditLogServiceImpl implements GvAuditLogService {

    private final GvAuditLogRepository gvAuditLogRepository;

    public GvAuditLogServiceImpl(GvAuditLogRepository gvAuditLogRepository) {
        this.gvAuditLogRepository = gvAuditLogRepository;
    }

    @Override
    @Async
    public void log(Long userId, String action, String entityName, Long entityId, String details, String ipAddress) {
        User user = null;
        if (userId != null) {
            user = new User();
            user.setId(userId);
        }

        GvAuditLog log = GvAuditLog.builder()
                .user(user)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .build();

        gvAuditLogRepository.save(log);
    }

    @Override
    public Page<GvAuditLog> findByEntity(String entityName, Long entityId, Pageable pageable) {
        return gvAuditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId, pageable);
    }

    @Override
    public Page<GvAuditLog> findByUser(Long userId, Pageable pageable) {
        return gvAuditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }
}
