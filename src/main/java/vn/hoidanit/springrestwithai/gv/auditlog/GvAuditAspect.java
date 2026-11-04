package vn.hoidanit.springrestwithai.gv.auditlog;

import java.lang.reflect.Method;
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

@Aspect
@Component
public class GvAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(GvAuditAspect.class);

    private final GvAuditLogService gvAuditLogService;

    public GvAuditAspect(GvAuditLogService gvAuditLogService) {
        this.gvAuditLogService = gvAuditLogService;
    }

    @Around("@annotation(gvAuditAction)")
    public Object audit(ProceedingJoinPoint joinPoint, GvAuditAction gvAuditAction) throws Throwable {
        String ipAddress = extractIpAddress();
        Long userId = extractUserId();

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            String details = buildDetails(gvAuditAction, userId, "FAILED", e.getMessage(), null);
            gvAuditLogService.log(userId, gvAuditAction.action(), gvAuditAction.entity(), null, details, ipAddress);
            throw e;
        }

        Long entityId = extractEntityId(result);
        String details = buildDetails(gvAuditAction, userId, "SUCCESS", null, entityId != null ? String.valueOf(entityId) : null);
        gvAuditLogService.log(userId, gvAuditAction.action(), gvAuditAction.entity(), entityId, details, ipAddress);

        return result;
    }

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
            log.warn("[GvAudit] Không thể lấy userId từ JWT: {}", e.getMessage());
        }
        return null;
    }

    private String extractIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
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

    private Long extractEntityId(Object result) {
        if (result == null) return null;
        Object payload = result;
        if (payload instanceof ResponseEntity<?> responseEntity) {
            payload = responseEntity.getBody();
        }
        if (payload instanceof ApiResponse<?> apiResponse) {
            payload = apiResponse.getData();
        }
        if (payload == null) return null;

        try {
            Method getId = payload.getClass().getMethod("getId");
            Object idObj = getId.invoke(payload);
            if (idObj instanceof Long l) return l;
            if (idObj instanceof Number n) return n.longValue();
        } catch (Exception ignored) {
            try {
                Method idMethod = payload.getClass().getMethod("id");
                Object idObj = idMethod.invoke(payload);
                if (idObj instanceof Long l) return l;
                if (idObj instanceof Number n) return n.longValue();
            } catch (Exception ignored2) {}
        }
        return null;
    }

    private String buildDetails(GvAuditAction action, Long userId, String status, String errorMsg, String entityId) {
        StringBuilder sb = new StringBuilder();
        sb.append("action=").append(action.action());
        sb.append(", entity=").append(action.entity());
        sb.append(", status=").append(status);
        if (action.description() != null && !action.description().isBlank()) {
            sb.append(", desc=").append(action.description());
        }
        if (errorMsg != null) {
            sb.append(", error=").append(errorMsg);
        }
        return sb.toString();
    }
}
