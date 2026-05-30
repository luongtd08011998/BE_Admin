package vn.hoidanit.springrestwithai.qlk.auditlog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu method cần ghi Audit Log.
 * <p>
 * AOP Aspect {@link QlkAuditAspect} sẽ intercept method này và tự động lưu log vào bảng qlk_audit_logs.
 *
 * <pre>
 * Ví dụ:
 *   @AuditAction(action = "CREATE_WAREHOUSE", entity = "Warehouse")
 *   public WarehouseResponse create(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {

    /**
     * Tên hành động, ví dụ: "CREATE_VOUCHER", "APPROVE_VOUCHER".
     */
    String action();

    /**
     * Tên entity bị tác động, ví dụ: "StockVoucher", "Warehouse".
     */
    String entity();

    /**
     * Mô tả tùy chọn (human-readable). Có thể để trống.
     */
    String description() default "";
}
