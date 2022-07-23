package com.fengx.saltedfish.service.impl;

import com.fengx.saltedfish.common.exception.WarnException;
import com.fengx.saltedfish.common.response.ObjectResponse;
import com.fengx.saltedfish.common.response.Response;
import com.fengx.saltedfish.common.response.SuccessResponse;
import com.fengx.saltedfish.model.entity.GameRoomInfo;
import com.fengx.saltedfish.model.entity.NettyMessage;
import com.fengx.saltedfish.model.enums.NettyMsgTypeEnum;
import com.fengx.saltedfish.model.enums.RoomTypeEnum;
import com.fengx.saltedfish.model.param.CreateRoomParam;
import com.fengx.saltedfish.model.param.GetGameInfoParam;
import com.fengx.saltedfish.model.param.RoomParam;
import com.fengx.saltedfish.server.GameManageServer;
import com.fengx.saltedfish.server.NettyHandlerServer;
import com.fengx.saltedfish.service.NettyGameService;
import com.fengx.saltedfish.utils.CopyUtil;
import com.fengx.saltedfish.utils.JsonUtil;
import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

@Service
public class NettyGameServiceImpl implements NettyGameService {

    @Override
    public Response roomList(String roomTypeEnum) {
        RoomTypeEnum value;
        try {
            value = RoomTypeEnum.valueOf(roomTypeEnum);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new WarnException("类型不存在！");
        }
        return new ObjectResponse<>(GameManageServer.getInstance().getRoomList(value.name()));
    }

    @Override
    public Response createRoom(CreateRoomParam param) {
        ChannelHandlerContext userCtx = NettyHandlerServer.getInstance().getUserCtx(param.getWsUserId());
        if (userCtx == null) {
            throw new WarnException("用户不存在！");
        }
        GameRoomInfo room = CopyUtil.beanCopy(param, new GameRoomInfo());
        room.setUserIds(Sets.newHashSet(param.getWsUserId()));
        if (!GameManageServer.getInstance().addRoom(param.getRoomTypeEnum(), room)) {
            throw new WarnException("创建房间失败！");
        }
        return new ObjectResponse<>(room);
    }

    @Override
    public Response quitRoom(RoomParam param) {
        if (!GameManageServer.getInstance().quitRoom(param.getRoomId(), param.getWsUserId())) {
            throw new WarnException("退出失败！");
        }
        return new SuccessResponse();
    }

    @Override
    public Response joinRoom(RoomParam param) {
        if (!GameManageServer.getInstance().joinRoom(param.getRoomId(), param.getWsUserId())) {
            throw new WarnException("加入失败！");
        }
        return new SuccessResponse();
    }

    @Override
    public Response inspectJoinRoom(String userId) {
        String roomId = GameManageServer.getInstance().inspectUserJoinRoom(userId);
        if (roomId != null) {
            return new ObjectResponse<>(GameManageServer.getInstance().getRoomInfo(roomId));
        }
        return new ObjectResponse<>(null);
    }

    @Override
    public Response getGameInfo(GetGameInfoParam param) {
        String gameInfo = GameManageServer.getInstance().getGameInfo(param);
        return new ObjectResponse<>(gameInfo);
    }
}
