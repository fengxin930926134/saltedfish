package com.fengx.saltedfish.task;

import com.fengx.saltedfish.server.NettyHandlerServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimedTask {

    @Scheduled(cron = "*/3 * * * * ?")
    public void inspectNettyMsgSendTask() {
        Integer againSendMsgTime = 3000;
        NettyHandlerServer.getInstance().inspectNettyMsgSend(againSendMsgTime);
    }

//    /**
//     * 检查房间是否需要销毁
//     */
//    @Scheduled(cron = "*/10 * * * * ?")
//    public void inspectRoomTask() {
//    }
}
