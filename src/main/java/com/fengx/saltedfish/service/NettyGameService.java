package com.fengx.saltedfish.service;

import com.fengx.saltedfish.common.response.Response;
import com.fengx.saltedfish.model.param.CreateRoomParam;
import com.fengx.saltedfish.model.param.GetGameInfoParam;
import com.fengx.saltedfish.model.param.RoomParam;

public interface NettyGameService {

    /**
     * 房间列表
     */
    Response roomList(String roomTypeEnum);

    /**
     * 创建房间
     */
    Response createRoom(CreateRoomParam param);

    /**
     * 退出房间
     */
    Response quitRoom(RoomParam param);

    /**
     * 加入房间
     */
    Response joinRoom(RoomParam param);

    /**
     * 检查userId是否已加入房间
     */
    Response inspectJoinRoom(String userId);

    /**
     * 获取游戏信息
     */
    Response getGameInfo(GetGameInfoParam param);
}
