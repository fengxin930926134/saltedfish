package com.fengx.saltedfish.model;

/**
 * 消息类型
 */
public enum NettyMsgTypeEnum {

    /**
     * 心跳请求
     */
    HEART_BEAT,

    /**
     * 回复消息，仅用确保消息被收到
     */
    REPLY,

    /**
     * 传递用户id
     */
    WS_USER_ID,

    /**
     * 更新房间人数
     */
    UPDATE_ROOM_NUMBER,

    /**
     * 开始游戏
     */
    BEGIN_GAME,
}
