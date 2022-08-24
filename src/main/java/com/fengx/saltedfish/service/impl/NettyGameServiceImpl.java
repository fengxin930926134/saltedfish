package com.fengx.saltedfish.service.impl;

import com.fengx.saltedfish.common.exception.WarnException;
import com.fengx.saltedfish.common.response.ObjectResponse;
import com.fengx.saltedfish.common.response.Response;
import com.fengx.saltedfish.common.response.SuccessResponse;
import com.fengx.saltedfish.model.entity.GameRoomInfo;
import com.fengx.saltedfish.model.entity.LandlordsGameInfo;
import com.fengx.saltedfish.model.entity.NettyMessage;
import com.fengx.saltedfish.model.enums.LandlordsHandCardsTypeEnum;
import com.fengx.saltedfish.model.enums.NettyMsgTypeEnum;
import com.fengx.saltedfish.model.enums.RoomTypeEnum;
import com.fengx.saltedfish.model.param.*;
import com.fengx.saltedfish.server.GameManageServer;
import com.fengx.saltedfish.server.NettyHandlerServer;
import com.fengx.saltedfish.service.NettyGameService;
import com.fengx.saltedfish.utils.CopyUtil;
import com.fengx.saltedfish.utils.JsonUtil;
import com.fengx.saltedfish.utils.LandlordsUtil;
import com.fengx.saltedfish.utils.TimerUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NettyGameServiceImpl implements NettyGameService {

    private final TimerUtil timerUtil;

    @Value("${game.landlord.countdown}")
    private Integer landlordCountdown;

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
        if (!GameManageServer.getInstance().joinRoom(param.getRoomId(), param.getWsUserId(), (gameInfo) -> {
            if (gameInfo instanceof LandlordsGameInfo) {
                LandlordsGameInfo landlordsGameInfo = (LandlordsGameInfo) gameInfo;
                startLandlordCountdown(landlordsGameInfo.getSorts().entrySet().stream()
                                .filter(e -> e.getValue().equals(landlordsGameInfo.getCurrentSort()))
                                .map(Map.Entry::getKey).collect(Collectors.toList()).get(0),
                        landlordsGameInfo.getCurrentSort(),
                        landlordsGameInfo);
            }
        })) {
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
                if (sort == 3 && beLandlordsMap[0] == 0) {
                    // 如果1没叫 说明现在是3成为地主
                    landlordsGameInfo.getSorts().forEach((userId, index) -> {
                        if (index == 3) {
                            landlordsGameInfo.setLandlord(userId);
                        }
                    });
                }
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
                if (sort == 1) {
                    int be = 1;
                    if (beLandlordsMap[2] == 1) {
                        be = 3;
                    }
                    int finalBe = be;
                    landlordsGameInfo.getSorts().forEach((userId, index) -> {
                        if (index == finalBe) {
                            landlordsGameInfo.setLandlord(userId);
                        }
                    });
                }
                break;
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

        // 停止上一个倒计时
        stopLandlordCountdown(landlordsGameInfo);
        // 未决出地主则切换下一人操作
        if (StringUtils.isBlank(landlordsGameInfo.getLandlord())) {
            landlordNextOperation(landlordsGameInfo);
        } else {
            // 决定出地主，通知开始出牌，设置地主开始出牌
            GameManageServer.getInstance().landlordBeginPlay(roomId);
            startLandlordCountdown(landlordsGameInfo.getLandlord(), landlordsGameInfo.getCurrentSort(), landlordsGameInfo);
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
        Integer sort = inspectUserOperate(landlordsGameInfo, param.getUserId());
        String content;
        boolean gameOver = false;
        if (param.getPlay()) {
            // 出牌
            if (CollectionUtils.isEmpty(param.getBrand())) {
                throw new WarnException("至少选中一张牌！");
            }
            // 检查是否合规
            LandlordsHandCardsTypeEnum typeEnum = LandlordsUtil.validationRules(param.getBrand(),
                    landlordsGameInfo.getCurrentOutCardSort() != null &&
                            landlordsGameInfo.getCurrentOutCardSort().equals(sort) ? null : landlordsGameInfo.getCurrentAlreadyOutCards());
            if (typeEnum.equals(LandlordsHandCardsTypeEnum.ERROR)) {
                throw new WarnException("出牌不合规则！");
            }
            // 把牌出掉
            param.getBrand().forEach(item -> landlordsGameInfo.getHandCards().get(param.getUserId()).remove(item));
            int size = landlordsGameInfo.getHandCards().get(param.getUserId()).size();
            content = param.getUserId() + "出牌  >>>  " + String.join("、", param.getBrand()) + " (" + typeEnum.getDesc() + ") [剩余" + size + "张牌]";
            // 更新已出牌序号和牌组
            landlordsGameInfo.setCurrentOutCardSort(landlordsGameInfo.getCurrentSort());
            landlordsGameInfo.setCurrentAlreadyOutCards(param.getBrand());
            // 如果出完牌了则游戏结束
            gameOver = size == 0;
        } else {
            if (landlordsGameInfo.getCurrentOutCardSort() == null ||
                    landlordsGameInfo.getCurrentOutCardSort().equals(landlordsGameInfo.getCurrentSort())) {
                throw new WarnException("先手出牌，不能过牌！");
            }
            content = param.getUserId() + "过牌";
        }
        landlordsGameInfo.getLog().add(content);
        GameManageServer.getInstance().pushLog(content, landlordsGameInfo.getHandCards().keySet());

        // 停止上一个倒计时
        stopLandlordCountdown(landlordsGameInfo);
        if (gameOver) {
            content = param.getUserId() + "取得胜利";
            landlordsGameInfo.getLog().add(content);
            GameManageServer.getInstance().pushLog(content, landlordsGameInfo.getHandCards().keySet());
            GameManageServer.getInstance().gameOver(roomId, landlordsGameInfo.getHandCards().keySet());
            // 退出房间
        } else {
            landlordNextOperation(landlordsGameInfo);
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

    /**
     * 下一位开始操作
     */
    private void landlordNextOperation(LandlordsGameInfo landlordsGameInfo) {
        GameManageServer.getInstance().nextOperation(landlordsGameInfo.getCurrentNumberMap(),
                landlordsGameInfo.getHandCards().keySet(), (nextSort) -> {
                    AtomicReference<String> id = new AtomicReference<>(null);

                    landlordsGameInfo.getSorts().forEach((userId, sort) -> {
                        if (sort.equals(nextSort)) {
                            id.set(userId);
                        }
                    });
                    // 通知
                    startLandlordCountdown(id.get(), nextSort, landlordsGameInfo);
                });
    }

    private void startLandlordCountdown(String userId, Integer nextSort, LandlordsGameInfo landlordsGameInfo) {
        if (userId != null) {
            AtomicInteger i = new AtomicInteger(landlordCountdown);
            landlordsGameInfo.setTimerId(timerUtil.startTask(() -> {
                NettyMessage message = new NettyMessage();
                message.setContent(userId + "开始操作，倒计时" + i.get() + "秒");
                message.setMsgType(NettyMsgTypeEnum.COUNT_DOWN);
                NettyHandlerServer.getInstance().sendAllMsgByIds(message, landlordsGameInfo.getHandCards().keySet());
                i.getAndDecrement();
            }, 1300, landlordCountdown * 1300, () -> {
                // 超时处理
                // 正在叫地主
                if (GameManageServer.getInstance().getBeLandlordsMap(landlordsGameInfo.getRoomId()) != null) {
                    BeLandlordParam param = new BeLandlordParam();
                    param.setUserId(userId);
                    param.setIsBeLandlord(false);
                    beLandlord(param);
                } else {
                    PlayBrandParam param = new PlayBrandParam();
                    param.setUserId(userId);
                    // 是出牌人
                    if (landlordsGameInfo.getCurrentOutCardSort() == null ||
                            landlordsGameInfo.getCurrentOutCardSort().equals(nextSort)) {
                        param.setBrand(Lists.newArrayList(landlordsGameInfo.getHandCards().get(userId).get(0)));
                        param.setPlay(true);
                        playBrand(param);
                        // 更新页面牌组
                        NettyMessage message = new NettyMessage();
                        message.setContent(JsonUtil.list2Json(landlordsGameInfo.getHandCards().get(userId)));
                        message.setMsgType(NettyMsgTypeEnum.LANDLORD_AUTO_PLAY);
                        NettyHandlerServer.getInstance().sendAllMsgByIds(message, Sets.newHashSet(userId));
                    } else {
                        // 过牌
                        param.setPlay(false);
                        playBrand(param);
                    }
                }
            }));
        }
    }

    private void stopLandlordCountdown(LandlordsGameInfo landlordsGameInfo) {
        if (landlordsGameInfo.getTimerId() != null) {
            timerUtil.stopTask(landlordsGameInfo.getTimerId());
            landlordsGameInfo.setTimerId(null);
        }
    }
}
