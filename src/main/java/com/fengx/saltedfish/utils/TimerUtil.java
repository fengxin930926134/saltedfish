package com.fengx.saltedfish.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimerUtil {

    private final ThreadPoolTaskScheduler scheduler;

    private static final Map<String, List<ScheduledFuture<?>>> TIMER_TASK = new Hashtable<>();

    public String startTask(Runnable task, long interval, long endTime, Runnable endHandle) {
        long currentTimeMillis = System.currentTimeMillis();
        String id = startTask(task, interval);
        ScheduledFuture<?> schedule = scheduler.schedule(() -> {
            stopTask(id);
            if (endHandle != null) {
                endHandle.run();
            }
        }, Instant.ofEpochMilli(currentTimeMillis + endTime + 10));
        List<ScheduledFuture<?>> orDefault = TIMER_TASK.getOrDefault(id, new ArrayList<>());
        orDefault.add(schedule);
        TIMER_TASK.put(id, orDefault);
        return id;
    }

    public String startTask(String taskId, Runnable task, long interval) {
        if (taskId == null) {
            taskId = RandomUtil.generateShortUuid();
        }
        List<ScheduledFuture<?>> orDefault = TIMER_TASK.getOrDefault(taskId, new ArrayList<>());
        orDefault.add(scheduler.scheduleAtFixedRate(task, interval));
        TIMER_TASK.put(taskId, orDefault);
        return taskId;
    }

    public String startTask(Runnable task, long interval) {
        return startTask(null, task, interval);
    }

    public void stopTask(String taskId) {
        stopTask(taskId, false);
    }

    public void stopTask(String taskId, boolean mayInterruptIfRunning) {
        if (StringUtils.isNotBlank(taskId)) {
            List<ScheduledFuture<?>> scheduledFutures = TIMER_TASK.get(taskId);
            if (CollectionUtils.isNotEmpty(scheduledFutures)) {
                scheduledFutures.forEach(item -> item.cancel(mayInterruptIfRunning));
                TIMER_TASK.remove(taskId);
            }
        }
    }
}
