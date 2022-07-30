package com.fengx.saltedfish.model.enums;

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

    /**
     * 推送日志
     */
    PUSH_LOG,

    /**
     * 切换下一人操作
     */
    NEXT_OPERATION,

    /**
     * 开始出牌
     */
    LANDLORD_BEGIN_PLAY,

    /**
     * 游戏结束
     */
    GAME_OVER,
}
