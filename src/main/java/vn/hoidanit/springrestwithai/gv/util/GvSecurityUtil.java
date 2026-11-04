package vn.hoidanit.springrestwithai.gv.util;

import java.util.Collection;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class GvSecurityUtil {

    private GvSecurityUtil() {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            Object claim = jwt.getClaim("userId");
            if (claim instanceof Number number) {
                return number.longValue();
            }
        }
        return null;
    }

    public static Long getRequiredCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện hành động này");
        }
        return userId;
    }

    public static boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        // 1. Kiểm tra authorities
        boolean hasAdminAuth = auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority().toUpperCase();
            return role.equals("ROLE_SUPER_ADMIN") || role.equals("SUPER_ADMIN")
                    || role.equals("ROLE_ADMIN") || role.equals("ADMIN");
        });
        if (hasAdminAuth) {
            return true;
        }

        // 2. Kiểm tra JWT claim "roles"
        if (auth.getPrincipal() instanceof Jwt jwt) {
            Object rolesClaim = jwt.getClaim("roles");
            if (rolesClaim instanceof Collection<?> roles) {
                for (Object o : roles) {
                    if (o != null) {
                        String r = o.toString().toUpperCase();
                        if (r.equals("ROLE_SUPER_ADMIN") || r.equals("SUPER_ADMIN")
                                || r.equals("ROLE_ADMIN") || r.equals("ADMIN")) {
                            return true;
                        }
                    }
                }
            } else if (rolesClaim instanceof String s) {
                String r = s.toUpperCase();
                return r.equals("ROLE_SUPER_ADMIN") || r.equals("SUPER_ADMIN")
                        || r.equals("ROLE_ADMIN") || r.equals("ADMIN");
            }
        }
        return false;
    }
}
