package com.fengx.saltedfish.server;

import com.fengx.saltedfish.model.entity.NettyMessage;
import com.fengx.saltedfish.model.enums.NettyMsgTypeEnum;
import com.fengx.saltedfish.utils.CopyUtil;
import com.fengx.saltedfish.utils.JsonUtil;
import com.fengx.saltedfish.utils.RandomUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 1、客户端心跳检测与服务端心跳检测
 * 服务端检测到某个客户端迟迟没有心跳过来能够主动关闭通道，让它下线； 客户端检测到某个服务端迟迟没有响应心跳也能重连获取一个新的链接
 *
 * 2、断网自动重连
 * 确保断网后，网络一旦恢复，能第一时间自动从新创建长链接（客户端重连）
 *
 * 3、消息确认
 * 客户端发送消息时携带客户端自己生成的消息id，服务端用这个做个幂等性，完成处理后携带客户端的消息id推送给客户端告知处理结果
 *
 * ps:所谓幂等性，就是一次和屡次请求一个接口都应该具备一样的后果。为何须要？对每一个接口的调用都会有三种可能的结果：成功，失败和超时。对最后一种的缘由不少多是网络丢包，可能请求没有到达，也有可能返回没有收到。因而在对接口的调用时每每都会有重试机制，但重试机制很容易致使消息的重复发送，从用户层面这每每是不可接受的，所以在接口的设计时，咱们就须要考虑接口的幂等性，确保同一条消息发送一次和十次都不回致使消息的重复到达
 *
 * 4、并发考虑
 * 服务端收到消息后，保存消息的任务推送到redis队列，使用job异步写入聊天信息。
 * ps:若是一层一层的同步调用下去，全部的调用方须要相同的等待时间，调用方的资源会被大量的浪费。更糟糕的是一旦被调用方出问题，其余调用就会出现多米诺骨牌效应跟着出问题，致使故障蔓延。收到请求当即返回结果，而后再异步执行，不只能够增长系统的吞吐量，最大的好处是让服务之间的解耦更为完全
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyHandlerServer extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static class Inner {
        private static final NettyHandlerServer INSTANCE = new NettyHandlerServer();
    }

    private NettyHandlerServer(){}

    public static NettyHandlerServer getInstance(){
        return Inner.INSTANCE;
    }

    /**
     * 保持连接的Context, k.ctx v.userId
     */
    public static final Map<ChannelHandlerContext, String> MAP = new HashMap<>();

    /**
     * kv相反，方便取数据
     */
    public static final Map<String, ChannelHandlerContext> MAPDATA = new Hashtable<>();

    /**
     * 用户退出时间数据
     * k.userId v.时间戳
     */
    public static final Map<String, Long> SIGNOUT_TIME_DATA = new Hashtable<>();

    /**
     * 已发送的消息列表 k.msgId v.消息
     */
    private static final Map<String, NettyMessage> SEND_MSG_MAP = new Hashtable<>();

    /**
     * 主动发送消息，赋值id
     *
     * @param message NettyMessage
     */
    public void sendMsg(NettyMessage message) {
        if (message.getCtx() != null) {
            message.setMsgId(RandomUtil.generateShortUuid());
            message.setSendTime(System.currentTimeMillis());
            send(message);
            SEND_MSG_MAP.put(message.getMsgId(), message);
        }
    }

    /**
     * 给所有在线用户发送消息
     *
     * @param message NettyMessage
     * @param ids userIds
     */
    public void sendAllMsgByIds(NettyMessage message, Collection<String> ids) {
        if (MAP.size() > 0 && CollectionUtils.isNotEmpty(ids)) {
            ids.forEach(userId -> {
                ChannelHandlerContext ctx = MAPDATA.get(userId);
                if (ctx != null) {
                    NettyMessage nettyMessage = CopyUtil.beanCopy(message, new NettyMessage());
                    nettyMessage.setCtx(ctx);
                    sendMsg(nettyMessage);
                }
            });
        }
    }

    /**
     * 给所有在线用户发送消息
     *
     * @param message NettyMessage
     */
    public void sendAllMsg(NettyMessage message) {
        if (MAP.size() > 0) {
            MAP.keySet().forEach(ctx -> {
                NettyMessage nettyMessage = CopyUtil.beanCopy(message, new NettyMessage());
                nettyMessage.setCtx(ctx);
                sendMsg(nettyMessage);
            });
        }
    }

    /**
     * 获取用户消息对象
     *
     * @param userId String
     */
    public ChannelHandlerContext getUserCtx(String userId) {
        if (userId != null) {
            if (MAP.containsValue(userId)) {
                return MAPDATA.get(userId);
            }
        }
        return null;
    }

    /**
     * 检查消息是否正常发送，没有则重发
     *
     * @param interval 毫秒
     */
    public void inspectNettyMsgSend(Integer interval) {
        if (interval != null && interval > 0) {
            long timeMillis = System.currentTimeMillis();
            SEND_MSG_MAP.forEach((id, msg) -> {
                if (msg.getSendTime() > timeMillis - interval) {
                    send(msg);
                }
            });
        }
    }

    /**
     * 获取退出连接数据
     */
    public Map<String, Long> getSignOutTimeData() {
        return SIGNOUT_TIME_DATA;
    }

    /**
     * 客户端上线的时候调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        MAP.put(ctx, null);
        log.info(ctx.channel().remoteAddress() + " 建立连接, 在线数:" + MAP.size());
        ctx.fireChannelActive();
    }

    /**
     * 客户端掉线的时候调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String remove = MAP.remove(ctx);
        if (remove != null) {
            MAPDATA.remove(remove);
            SIGNOUT_TIME_DATA.put(remove, System.currentTimeMillis());
        }
        log.info(ctx.channel().remoteAddress() + " 断开连接, 在线数:" + MAP.size());
        ctx.fireChannelInactive();
    }


    /**
     * 读取客户端信息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame message) {
        String remoteAddress = ctx.channel().remoteAddress().toString();
        log.info("receive {} -> {}", remoteAddress, message.text());
        if (!MAP.containsKey(ctx)) {
            log.error("ip:" + remoteAddress + " 不存在此数据，断开连接...");
            ctx.close();
        }
        try {
            NettyMessage nettyMessage = JsonUtil.json2Object(message.text(), NettyMessage.class);
            if (NettyMsgTypeEnum.REPLY.equals(nettyMessage.getMsgType())) {
                // 客户端正常回复消息，无需处理
                SEND_MSG_MAP.remove(nettyMessage.getMsgId());
            } else {
                // 先发送回复消息
                NettyMessage reply = CopyUtil.beanCopy(nettyMessage, new NettyMessage());
                reply.setErrmsg("0000");
                reply.setErrcode("ok");
                reply.setContent(null);
                reply.setMsgType(NettyMsgTypeEnum.REPLY);
                reply.setCtx(ctx);
                send(reply);
                // 处理消息
                switch (nettyMessage.getMsgType()) {
                    case REPLY:
                    case WS_USER_ID:
                        break;
                    // 检查ws用户id，不存在则生成
                    case HEART_BEAT:
                        if (StringUtils.isBlank(nettyMessage.getContent())) {
                            nettyMessage.setContent(RandomUtil.generateShortUuid());
                            nettyMessage.setMsgType(NettyMsgTypeEnum.WS_USER_ID);
                            nettyMessage.setCtx(ctx);
                            sendMsg(nettyMessage);
                        }
                        // 关联id
                        MAP.put(ctx, nettyMessage.getContent());
                        MAPDATA.put(nettyMessage.getContent(), ctx);
                        break;
                    default: ctx.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.close();
        }

    }

    /**
     * 异常发生时候调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    /**
     * 发送消息
     */
    private void send(NettyMessage message) {
        NettyMessage nettyMessage = CopyUtil.beanCopy(message, new NettyMessage());
        nettyMessage.setCtx(null);
        String msg = JsonUtil.object2Json(nettyMessage);
        log.info("send -> " + msg);
        message.getCtx().writeAndFlush(new TextWebSocketFrame(msg));
    }
}