package com.fengx.saltedfish.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步任务
 */
@Slf4j
@Component
public class AsyncTask {
	
	@Async
	public void doTask() throws InterruptedException{
	}
}