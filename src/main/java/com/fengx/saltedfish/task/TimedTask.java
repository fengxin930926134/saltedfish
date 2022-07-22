package com.fengx.saltedfish.task;

import com.fengx.saltedfish.server.GameManageServer;
import com.fengx.saltedfish.server.NettyHandlerServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimedTask {

    @Scheduled(cron = "*/3 * * * * ?")
    public void inspectNettyMsgSendTask() {
        int againSendMsgTime = 3000;
        NettyHandlerServer.getInstance().inspectNettyMsgSend(againSendMsgTime);
    }

    /**
     * 检查房间是否需要销毁
     */
    @Scheduled(cron = "*/20 * * * * ?")
    public void inspectRoomTask() {
        int signOutTime = 20000;
        Set<String> rml = new HashSet<>();
        Map<String, Long> signOutTimeData = NettyHandlerServer.getInstance().getSignOutTimeData();
        long timeMillis = System.currentTimeMillis();
        signOutTimeData.forEach((k, v) -> {
            if (timeMillis - v > signOutTime) {
                String joinRoom = GameManageServer.getInstance().inspectUserJoinRoom(k);
                // 退出和清空
                if (joinRoom != null) {
                    GameManageServer.getInstance().quitRoom(joinRoom, k);
                }
                rml.add(k);
            }
        });
        if (CollectionUtils.isNotEmpty(rml)) {
            rml.forEach(signOutTimeData::remove);
        }
    }
}
