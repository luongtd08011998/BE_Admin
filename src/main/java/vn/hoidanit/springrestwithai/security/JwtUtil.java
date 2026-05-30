package vn.hoidanit.springrestwithai.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import vn.hoidanit.springrestwithai.exception.BadRequestException;

/**
 * Utility class để extract thông tin từ JWT token trong SecurityContext.
 */
public final class JwtUtil {

    private JwtUtil() {}

    /**
     * Lấy warehouseId từ JWT claim của user đang đăng nhập.
     * Ném BadRequestException nếu user chưa chọn kho làm việc.
     */
    public static Long extractWarehouseId() {
        Long warehouseId = extractWarehouseIdOrNull();
        if (warehouseId == null) {
            throw new BadRequestException("Bạn chưa chọn kho làm việc. Vui lòng đăng nhập lại với kho cụ thể.");
        }
        return warehouseId;
    }

    /**
     * Lấy warehouseId từ JWT, trả về null nếu không có (không ném exception).
     */
    public static Long extractWarehouseIdOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            Object claim = jwtToken.getToken().getClaim("warehouseId");
            if (claim instanceof Number number) {
                return number.longValue();
            }
        }
        return null;
    }
}
