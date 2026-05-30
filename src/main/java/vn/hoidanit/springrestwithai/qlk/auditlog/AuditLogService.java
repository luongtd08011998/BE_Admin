package vn.hoidanit.springrestwithai.qlk.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void log(Long userId, String action, String entityName, Long entityId, String details, String ipAddress);

    Page<AuditLog> findByUser(Long userId, Pageable pageable);

    Page<AuditLog> findByEntity(String entityName, Long entityId, Pageable pageable);

    Page<AuditLog> findWithFilters(String entityName, String action, Long userId, Pageable pageable);
}

