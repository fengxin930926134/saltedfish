package com.fengx.saltedfish.config.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *  线程池配置
 *
 *  JDK线程池
 * （1）ExecutorService
 * （2）ScheduledExecutorService
 * Spring线程池
 * （1）ThreadPoolTaskExecutor
 * （2）ThreadPoolTaskSchedular
 * 分布式定时任务
 * Spring Quartz
 *
 * 线程池有关内容整体了解：
 * JDK自带了线程池ExecutorService普通线程池，ScheduledExecutorService创建定时任务。
 * spring框架的线程池ThreadPoolTaskExecutor普通线程池，ThreadPoolTaskSchedular创建定时任务。
 * JDK的线程池在分布式环境下有问题（因为它数据是存在内存的），所以我们一般用Spring Quartz（官网http://www.quartz-schedular.org）
 *
 * CPU 密集型任务：
 * 比如像加解密，压缩、计算等一系列需要大量耗费 CPU 资源的任务，大部分场景下都是纯 CPU 计算。
 * 配置：CPU 核数 + 1
 *
 * IO 密集型任务：
 * 比如像 MySQL 数据库、文件的读写、网络通信等任务，这类任务不会特别消耗 CPU 资源，但是 IO 操作比较耗时，会占用比较多时间
 * 配置：CPU 核数 * (1 + IO 耗时/ CPU 耗时)
 * 如果任务的平均等待时间长，线程数就随之增加
 *
 * 获取核数：Runtime.getRuntime().availableProcessors()
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService executorService(){
        return new ThreadPoolExecutor(
                5,
                20,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200), new ThreadFactoryBuilder().setNameFormat("my-thread").build());
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(100);
        executor.setThreadNamePrefix("scheduler-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(30);
        executor.setKeepAliveSeconds(60);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}