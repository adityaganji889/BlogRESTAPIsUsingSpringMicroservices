package com.services.UserService.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {
	
	@Override
    @Bean(name = "asyncTaskExecutor")
	public Executor getAsyncExecutor()  {
		
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		
		// Below set properties depends on OS capability, for Windows 10, Intel Core i5 8th gen, 8GB DDR4 RAM, 128 GB SSD, 1 TB HDD, 2GB Graphics Card.
		threadPoolTaskExecutor.setCorePoolSize(1000);
//		threadPoolTaskExecutor.setQueueCapacity(20); Keeping it commented else throws error if queue is full.
		threadPoolTaskExecutor.setMaxPoolSize(1000);
		threadPoolTaskExecutor.setThreadNamePrefix("AsyncTaskExecutor(User-Service)--");
		
		threadPoolTaskExecutor.initialize();
		// To include security context with async methods
		return new DelegatingSecurityContextAsyncTaskExecutor(threadPoolTaskExecutor);
		
	}
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, obj) -> {
            System.err.println("Exception in async method: " + method.getName());
            throwable.printStackTrace();
        };
    }
}
