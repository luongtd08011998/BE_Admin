package vn.hoidanit.springrestwithai.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import vn.hoidanit.springrestwithai.feature.permission.Permission;
import vn.hoidanit.springrestwithai.feature.role.Role;
import vn.hoidanit.springrestwithai.feature.role.RoleRepository;

@Component
public class PermissionAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final RoleRepository roleRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Cache: roleName → List<Permission>
    // Load 1 lần, invalidate khi admin thay đổi
    private Map<String, List<Permission>> rolePermissionsCache;

    public PermissionAuthorizationManager(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        loadCache();
    }

    public void loadCache() {
        // 1 query JOIN FETCH tất cả roles + permissions
        List<Role> roles = roleRepository.findAllWithPermissions();

        Map<String, List<Permission>> cache = new HashMap<>();
        for (Role role : roles) {
            cache.computeIfAbsent("ROLE_" + role.getName(), k -> new ArrayList<>())
                    .addAll(role.getPermissions());
        }
        this.rolePermissionsCache = cache;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PermissionAuthorizationManager.class);

    @Override
    public AuthorizationDecision authorize(
            Supplier<? extends Authentication> authSupplier,
            RequestAuthorizationContext context) {

        Authentication authentication = authSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        String requestPath = context.getRequest().getRequestURI();
        String httpMethod = context.getRequest().getMethod();

        // Lấy roles của user từ JWT hoặc SecurityContext
        List<String> userRoles = getUserRolesFromJwt(authentication);

        // Bypass toàn bộ quyền cho SUPER_ADMIN hoặc ADMIN
        if (isAdmin(authentication, userRoles)) {
            return new AuthorizationDecision(true);
        }

        // Với mỗi role → lấy permissions từ cache → check match
        for (String role : userRoles) {
            List<Permission> permissions = rolePermissionsCache
                    .getOrDefault(role, Collections.emptyList());

            for (Permission perm : permissions) {
                if (perm.getMethod().equalsIgnoreCase(httpMethod)
                        && pathMatcher.match(perm.getApiPath(), requestPath)) {
                    return new AuthorizationDecision(true);
                }
            }
        }

        log.warn("Permission denied: path={} method={} roles={} cachedRoles={}",
                requestPath, httpMethod, userRoles, rolePermissionsCache.keySet());
        return new AuthorizationDecision(false);
    }

    /**
     * JWT claim "roles" may be List, JSONArray-backed Collection, etc. — normalize to strings.
     * Fallback sang authentication.getAuthorities() nếu không có trong claim JWT.
     */
    private List<String> getUserRolesFromJwt(Authentication authentication) {
        if (authentication == null) {
            return Collections.emptyList();
        }
        if (!(authentication instanceof JwtAuthenticationToken jwtToken)) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        }
        Object rolesClaim = jwtToken.getToken().getClaim("roles");
        if (rolesClaim == null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
        }
        if (rolesClaim instanceof Collection<?> roles) {
            List<String> out = new ArrayList<>();
            for (Object o : roles) {
                if (o != null) {
                    out.add(o.toString());
                }
            }
            return out;
        }
        if (rolesClaim instanceof String s && !s.isBlank()) {
            return List.of(s);
        }
        return Collections.emptyList();
    }

    /**
     * Dùng cho @PreAuthorize("@permissionAuthorizationManager.hasPermission(...)")
     */
    public boolean hasPermission(String permissionName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        List<String> userRoles = getUserRolesFromJwt(authentication);
        // Bypass cho SUPER_ADMIN hoặc ADMIN
        if (isAdmin(authentication, userRoles)) {
            return true;
        }

        for (String role : userRoles) {
            List<Permission> permissions = rolePermissionsCache.getOrDefault(role, Collections.emptyList());
            for (Permission perm : permissions) {
                if (perm.getName().equalsIgnoreCase(permissionName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem user có phải là SUPER_ADMIN hoặc ADMIN hay không.
     */
    private boolean isAdmin(Authentication authentication, List<String> userRoles) {
        if (userRoles != null && userRoles.stream().anyMatch(this::isAdminRoleName)) {
            return true;
        }
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(a -> a != null && isAdminRoleName(a.getAuthority()));
        }
        return false;
    }

    private boolean isAdminRoleName(String roleName) {
        if (roleName == null) return false;
        String r = roleName.trim().toUpperCase();
        return r.equals("ROLE_SUPER_ADMIN") || r.equals("SUPER_ADMIN")
                || r.equals("ROLE_ADMIN") || r.equals("ADMIN");
    }
}
