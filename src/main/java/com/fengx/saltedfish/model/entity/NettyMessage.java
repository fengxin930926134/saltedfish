package com.fengx.saltedfish.model.entity;

import com.fengx.saltedfish.model.enums.NettyMsgTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * 消息体
 */
@Data
public class NettyMessage {

    /**
     * 消息标识
     */
    private String msgId;

    /**
     * 0000 成功
     */
    private String errcode;

    /**
     * 错误提示信息
     */
    private String errmsg;

    /**
     * 消息类型
     */
    private NettyMsgTypeEnum msgType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 当前时间戳
     */
    private Long sendTime;

    /**
     * 接收人
     */
    private ChannelHandlerContext ctx;
}
