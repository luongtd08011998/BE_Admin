package vn.hoidanit.springrestwithai.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.hoidanit.springrestwithai.feature.log.SystemLogService;

import java.util.Arrays;

@Aspect
@Component
public class AdminActivityAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminActivityAspect.class);
    private final SystemLogService systemLogService;

    public AdminActivityAspect(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    @Pointcut("execution(* vn.hoidanit.springrestwithai..*Controller.*(..))")
    public void restControllerPointcut() {}

    @Around("restControllerPointcut()")
    public Object logAdminActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String method = request.getMethod();

        // Chỉ log các thao tác thay đổi dữ liệu (POST, PUT, DELETE)
        if (!Arrays.asList("POST", "PUT", "DELETE").contains(method.toUpperCase())) {
            return joinPoint.proceed();
        }

        String url = request.getRequestURI();
        String ipAddress = request.getRemoteAddr();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser"))
                ? auth.getName()
                : "anonymousUser";

        // Đặc biệt xử lý cho login request nếu email đang là anonymous
        if ("anonymousUser".equals(userEmail)) {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof vn.hoidanit.springrestwithai.feature.auth.dto.LoginRequest loginRequest) {
                    userEmail = loginRequest.email();
                    break;
                }
            }
        }

        String action = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String argsString = maskSensitiveData(args);

        String description = String.format("%s request to %s via %s | Args: %s", method, url, action, argsString);

        Object result;
        try {
            result = joinPoint.proceed();
            systemLogService.logAdminAction(userEmail, action, "SUCCESS", description, argsString, ipAddress);
            return result;
        } catch (Exception e) {
            systemLogService.logAdminAction(userEmail, action, "FAILURE", description + " - Error: " + e.getMessage(), argsString, ipAddress);
            throw e;
        }
    }

    private String maskSensitiveData(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else if (arg instanceof vn.hoidanit.springrestwithai.feature.auth.dto.LoginRequest) {
                // Không log mật khẩu của request đăng nhập
                vn.hoidanit.springrestwithai.feature.auth.dto.LoginRequest loginReq = (vn.hoidanit.springrestwithai.feature.auth.dto.LoginRequest) arg;
                sb.append(String.format("LoginRequest(email=%s, password=***)", loginReq.email()));
            } else {
                sb.append(arg.toString());
            }
            if (i < args.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
