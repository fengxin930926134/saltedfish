package com.fengx.saltedfish.model.entity;

import com.fengx.saltedfish.model.enums.RoomTypeEnum;
import lombok.Data;

import java.util.Set;

/**
 * 游戏房间
 */
@Data
public class GameRoomInfo {

    private String id;
    private String roomName;
    /**
     * 房间类型
     */
    private RoomTypeEnum roomTypeEnum;
    /**
     * socket生成的临时id
     */
    private Set<String> userIds;
    /**
     * 房间能容纳总人数
     */
    private Integer number;
    /**
     * true.游戏中 false.等待中
     */
    private Boolean playing;
}
