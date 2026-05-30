package vn.hoidanit.springrestwithai.qlk.auditlog;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.hoidanit.springrestwithai.dto.ApiResponse;

import java.lang.reflect.Method;

/**
 * AOP Aspect tự động ghi Audit Log cho module QLK.
 * <p>
 * Intercept tất cả method được đánh dấu {@link AuditAction} và lưu log bất đồng bộ.
 * <ul>
 *   <li>userId: lấy từ JWT claim "userId"</li>
 *   <li>entityId: lấy từ trường "id" của đối tượng trả về (qua reflection)</li>
 *   <li>details: JSON-like string chứa action + entity + userId</li>
 *   <li>ipAddress: lấy từ HttpServletRequest</li>
 * </ul>
 */
@Aspect
@Component
public class QlkAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(QlkAuditAspect.class);

    private final AuditLogService auditLogService;

    public QlkAuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(auditAction)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        // Lấy thông tin request
        String ipAddress = extractIpAddress();
        Long userId = extractUserId();

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            // Ghi log FAILED nếu method throw exception
            String details = buildDetails(auditAction, userId, "FAILED", e.getMessage(), null);
            auditLogService.log(userId, auditAction.action(), auditAction.entity(), null, details, ipAddress);
            throw e;
        }

        // Lấy entityId từ kết quả trả về (nếu có)
        Long entityId = extractEntityId(result);
        String entityCode = extractEntityCode(result);

        String details = buildDetails(auditAction, userId, "SUCCESS", null, entityCode);
        auditLogService.log(userId, auditAction.action(), auditAction.entity(), entityId, details, ipAddress);

        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper methods
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Lấy userId từ JWT trong SecurityContext.
     * Trả về null nếu chưa xác thực hoặc không có claim "userId".
     */
    private Long extractUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                Object userIdClaim = jwt.getClaim("userId");
                if (userIdClaim instanceof Long l) return l;
                if (userIdClaim instanceof Integer i) return i.longValue();
                if (userIdClaim instanceof Number n) return n.longValue();
            }
        } catch (Exception e) {
            log.warn("[QlkAudit] Không thể lấy userId từ JWT: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Lấy IP từ HttpServletRequest hiện tại.
     */
    private String extractIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Trích xuất entityId từ kết quả trả về của method.
     * <p>
     * Hỗ trợ:
     * <ul>
     *   <li>{@link ResponseEntity} bọc {@link ApiResponse} bọc object có trường {@code id}</li>
     *   <li>Object trực tiếp có trường {@code id}</li>
     * </ul>
     */
    private Long extractEntityId(Object result) {
        if (result == null) return null;
        try {
            Object unwrapped = result;

            // Unwrap ResponseEntity
            if (unwrapped instanceof ResponseEntity<?> re) {
                unwrapped = re.getBody();
            }

            // Unwrap ApiResponse
            if (unwrapped instanceof ApiResponse<?> ar) {
                unwrapped = ar.getData();
            }

            if (unwrapped == null) return null;

            // Lấy field "id" qua reflection
            return getIdField(unwrapped);
        } catch (Exception e) {
            log.debug("[QlkAudit] Không thể trích xuất entityId: {}", e.getMessage());
            return null;
        }
    }

    private Long getIdField(Object obj) {
        if (obj == null) return null;
        try {
            // Tìm method getId() (thường được Lombok/record generate)
            Method getId = obj.getClass().getMethod("getId");
            Object idVal = getId.invoke(obj);
            if (idVal instanceof Long l) return l;
            if (idVal instanceof Integer i) return i.longValue();
            if (idVal instanceof Number n) return n.longValue();
        } catch (NoSuchMethodException ignored) {
            // Không có getId(), thử field trực tiếp
            try {
                var field = obj.getClass().getDeclaredField("id");
                field.setAccessible(true);
                Object idVal = field.get(obj);
                if (idVal instanceof Long l) return l;
                if (idVal instanceof Integer i) return i.longValue();
                if (idVal instanceof Number n) return n.longValue();
            } catch (Exception ignored2) {
                // Không quan trọng
            }
        } catch (Exception e) {
            log.debug("[QlkAudit] getIdField error: {}", e.getMessage());
        }
        return null;
    }

    private String extractEntityCode(Object result) {
        if (result == null) return null;
        try {
            Object unwrapped = result;
            if (unwrapped instanceof ResponseEntity<?> re) {
                unwrapped = re.getBody();
            }
            if (unwrapped instanceof ApiResponse<?> ar) {
                unwrapped = ar.getData();
            }
            if (unwrapped == null) return null;
            return getCodeField(unwrapped);
        } catch (Exception e) {
            return null;
        }
    }

    private String getCodeField(Object obj) {
        if (obj == null) return null;
        String[] methodNames = {"getVoucherCode", "getCode", "getSku"};
        for (String methodName : methodNames) {
            try {
                Method method = obj.getClass().getMethod(methodName);
                Object val = method.invoke(obj);
                if (val instanceof String s && !s.isBlank()) return s;
            } catch (Exception ignored) {}
        }
        String[] fieldNames = {"voucherCode", "code", "sku"};
        for (String fieldName : fieldNames) {
            try {
                var field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object val = field.get(obj);
                if (val instanceof String s && !s.isBlank()) return s;
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Tạo chuỗi details để lưu vào audit log.
     */
    private String buildDetails(AuditAction auditAction, Long userId, String status, String errorMsg, String entityCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("action=").append(auditAction.action());
        sb.append(", entity=").append(auditAction.entity());
        sb.append(", userId=").append(userId);
        sb.append(", status=").append(status);
        if (auditAction.description() != null && !auditAction.description().isBlank()) {
            sb.append(", desc=").append(auditAction.description());
        }
        if (errorMsg != null) {
            sb.append(", error=").append(errorMsg);
        }
        if (entityCode != null) {
            sb.append(", code=").append(entityCode);
        }
        return sb.toString();
    }
}
