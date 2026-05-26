package vn.hoidanit.springrestwithai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Cấu hình thread pool cho @Scheduled tasks.
 * Mặc định Spring chỉ dùng 1 thread cho tất cả scheduled tasks —
 * nếu 1 task bị block (VD: Firebase timeout), tất cả các task khác đều dừng.
 * Pool 3 thread: 1 cho PaymentNotify, 1 cho InvoiceNotify, 1 dự phòng.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("scheduling-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }
}
