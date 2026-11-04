package vn.hoidanit.springrestwithai.gv.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GvAuditLogService {

    void log(Long userId, String action, String entityName, Long entityId, String details, String ipAddress);

    Page<GvAuditLog> findByEntity(String entityName, Long entityId, Pageable pageable);

    Page<GvAuditLog> findByUser(Long userId, Pageable pageable);
}
