package vn.hoidanit.springrestwithai.qlk.auditlog;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long userId,
        String userEmail,
        String action,
        String entityName,
        Long entityId,
        String details,
        String ipAddress,
        Instant timestamp
) {
    public static AuditLogResponse from(AuditLog log) {
        String email = (log.getUser() != null) ? log.getUser().getEmail() : null;
        Long uid = (log.getUser() != null) ? log.getUser().getId() : null;
        return new AuditLogResponse(
                log.getId(),
                uid,
                email,
                log.getAction(),
                log.getEntityName(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getTimestamp()
        );
    }
}
