package com.fengx.saltedfish.service.impl;

import com.fengx.saltedfish.common.exception.WarnException;
import com.fengx.saltedfish.common.response.ObjectResponse;
import com.fengx.saltedfish.common.response.Response;
import com.fengx.saltedfish.common.response.SuccessResponse;
import com.fengx.saltedfish.model.entity.GameRoomInfo;
import com.fengx.saltedfish.model.entity.LandlordsGameInfo;
import com.fengx.saltedfish.model.enums.RoomTypeEnum;
import com.fengx.saltedfish.model.param.*;
import com.fengx.saltedfish.server.GameManageServer;
import com.fengx.saltedfish.server.NettyHandlerServer;
import com.fengx.saltedfish.service.NettyGameService;
import com.fengx.saltedfish.utils.CopyUtil;
import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
        if (gameInfo == null) {
            throw new WarnException("游戏信息不存在！");
        }
        return new ObjectResponse<>(gameInfo);
    }

    @Override
    public Response beLandlord(BeLandlordParam param) {
        String roomId = GameManageServer.getInstance().inspectUserJoinRoom(param.getUserId());
        if (roomId == null) {
            throw new WarnException("未加入房间！");
        }
        LandlordsGameInfo landlordsGameInfo = GameManageServer.getInstance().getLandlordsGameInfo(roomId);
        int sort = inspectUserOperate(landlordsGameInfo, param.getUserId());
        // 序号权重
        Integer[] beLandlordsMap = GameManageServer.getInstance().getBeLandlordsMap(roomId);
        int weight = beLandlordsMap[sort - 1];
        int zong = 0;
        for (Integer integer : beLandlordsMap) {
            zong = integer + zong;
        }
        // 推送日志，操作次数+1
        String content;
        if (zong == 0) {
            if (param.getIsBeLandlord()) {
                // 叫地主
                beLandlordsMap[sort - 1] = weight + 1;
                zong++;
                content = "玩家 " + param.getUserId() + " 叫地主";
            } else {
                // 不叫
                content = "玩家 " + param.getUserId() + " 不叫地主";
            }
        } else {
            if (param.getIsBeLandlord()) {
                // 抢地主
                beLandlordsMap[sort - 1] = weight + 1;
                zong++;
                content = "玩家 " + param.getUserId() + " 抢地主";
            } else {
                // 不抢
                content = "玩家 " + param.getUserId() + " 不抢地主";
            }
        }
        landlordsGameInfo.getLog().add(content);
        GameManageServer.getInstance().pushLog(content, landlordsGameInfo.getHandCards().keySet());

        // 判断这次操作后的结局
        switch (zong) {
            case 0:
                if (sort == 3) {
                    // 重开
                    GameManageServer.getInstance()
                            .landlordRefreshGame(roomId, landlordsGameInfo.getHandCards().keySet());
                }
                break;
            case 1:
                if (sort == 3) {
                    if (param.getIsBeLandlord()) {
                        landlordsGameInfo.setLandlord(param.getUserId());
                    } else {
                        int be = 1;
                        if (beLandlordsMap[1] > 0) {
                            be = 2;
                        }
                        int finalBe = be;
                        landlordsGameInfo.getSorts().forEach((userId, index) -> {
                            if (index == finalBe) {
                                landlordsGameInfo.setLandlord(userId);
                            }
                        });
                    }
                }
                break;
            case 2:
                if (sort == 1) {
                    // 说明有人抢了但1没抢
                    int be = 3;
                    if (beLandlordsMap[1] > 0) {
                        be = 2;
                    }
                    int finalBe = be;
                    landlordsGameInfo.getSorts().forEach((userId, index) -> {
                        if (index == finalBe) {
                            landlordsGameInfo.setLandlord(userId);
                        }
                    });
                }
                break;
            case 3:
            case 4:
                if (sort == 1) {
                    // 设置序号1为地主
                    landlordsGameInfo.getSorts().forEach((userId, index) -> {
                        if (index == 1) {
                            landlordsGameInfo.setLandlord(userId);
                        }
                    });
                }
                break;
            default:
        }

        // 未决出地主则切换下一人操作
        if (StringUtils.isBlank(landlordsGameInfo.getLandlord())) {
            GameManageServer.getInstance().nextOperation(landlordsGameInfo.getCurrentNumberMap(),
                    landlordsGameInfo.getHandCards().keySet());
        } else {
            // 决定出地主，通知开始出牌，设置地主开始出牌
            GameManageServer.getInstance().landlordBeginPlay(roomId);
        }
        return new SuccessResponse();
    }

    @Override
    public Response playBrand(PlayBrandParam param) {
        String roomId = GameManageServer.getInstance().inspectUserJoinRoom(param.getUserId());
        if (roomId == null) {
            throw new WarnException("未加入房间！");
        }
        LandlordsGameInfo landlordsGameInfo = GameManageServer.getInstance().getLandlordsGameInfo(roomId);
        inspectUserOperate(landlordsGameInfo, param.getUserId());
        String content;
        boolean gameOver = false;
        if (param.getPlay()) {
            // 出牌
            if (CollectionUtils.isEmpty(param.getBrand())) {
                throw new WarnException("至少选中一张牌！");
            }
            // TODO 检查是否合规
            landlordsGameInfo.getHandCards().get(param.getUserId()).removeAll(param.getBrand());
            int size = landlordsGameInfo.getHandCards().get(param.getUserId()).size();
            content = param.getUserId() + "出牌  >>>  " + String.join("、", param.getBrand()) + "  [剩余" + size +"张牌]";
            // 如果出完牌了则游戏结束
            gameOver = size == 0;
        } else {
            // 如果是刚开始出牌则必须出
            if (landlordsGameInfo.getHandCards().get(param.getUserId()).size() > 17) {
                throw new WarnException("必须出牌！");
            }
            content = param.getUserId() + "过牌";
        }
        landlordsGameInfo.getLog().add(content);
        GameManageServer.getInstance().pushLog(content, landlordsGameInfo.getHandCards().keySet());

        if (gameOver) {
            content = param.getUserId() + "取得胜利";
            landlordsGameInfo.getLog().add(content);
            GameManageServer.getInstance().pushLog(content, landlordsGameInfo.getHandCards().keySet());
            GameManageServer.getInstance().gameOver(roomId, landlordsGameInfo.getHandCards().keySet());
            // 退出房间
        } else {
            GameManageServer.getInstance().nextOperation(landlordsGameInfo.getCurrentNumberMap(),
                    landlordsGameInfo.getHandCards().keySet());
        }

        return new SuccessResponse();
    }

    private Integer inspectUserOperate(LandlordsGameInfo landlordsGameInfo, String userId) {
        // 当前用户的序号
        Integer sort = landlordsGameInfo.getSorts().get(userId);
        if (sort == null) {
            throw new WarnException("数据有误！");
        }
        if (!sort.equals(landlordsGameInfo.getCurrentSort())) {
            throw new WarnException("还没到你操作！");
        }
        return sort;
    }
}
