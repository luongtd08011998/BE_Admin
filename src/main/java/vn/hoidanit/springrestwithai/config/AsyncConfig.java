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
 * <p>Core pool = 5, max = 10 thread đồng thời → gửi song song 10 FCM call thay vì tuần tự.
 * Queue capacity = 500 → đệm đủ cho burst lớn (8.000 KH/tháng).
 * Nếu queue đầy → CallerRunsPolicy → thread gọi sẽ tự gửi (không mất message).
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "fcmTaskExecutor")
    public Executor fcmTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("fcm-push-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        log.info("FCM async thread pool initialized: core=5, max=10, queue=500");
        return executor;
    }
}
