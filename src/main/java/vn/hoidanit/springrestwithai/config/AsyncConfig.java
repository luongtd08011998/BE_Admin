package vn.hoidanit.springrestwithai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Cấu hình thread pool cho các tác vụ bất đồng bộ (FCM push notification).
 *
 * <p>Core pool = 20, max = 50 thread đồng thời → gửi song song nhiều FCM call.
 * Queue capacity = 5000 → đệm đủ cho burst lớn (8.000 KH/tháng).
 * CallerRunsPolicy bị bỏ → dùng DiscardPolicy để không block scheduler thread.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "fcmTaskExecutor")
    public Executor fcmTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("fcm-push-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        log.info("FCM async thread pool initialized: core=20, max=50, queue=5000");
        return executor;
    }
}
