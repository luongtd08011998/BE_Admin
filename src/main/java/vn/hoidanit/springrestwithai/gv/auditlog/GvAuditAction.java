package vn.hoidanit.springrestwithai.gv.auditlog;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GvAuditAction {

    String action();

    String entity() default "GvTask";

    String description() default "";
}
