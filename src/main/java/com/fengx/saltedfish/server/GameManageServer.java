package com.fengx.saltedfish.server;

import com.fengx.saltedfish.common.exception.WarnException;
import com.fengx.saltedfish.model.entity.GameRoomInfo;
import com.fengx.saltedfish.model.entity.LandlordsGameInfo;
import com.fengx.saltedfish.model.entity.NettyMessage;
import com.fengx.saltedfish.model.enums.NettyMsgTypeEnum;
import com.fengx.saltedfish.model.enums.RoomTypeEnum;
import com.fengx.saltedfish.model.param.GetGameInfoParam;
import com.fengx.saltedfish.model.vo.LandlordsGameInfoVO;
import com.fengx.saltedfish.utils.JsonUtil;
import com.fengx.saltedfish.utils.LandlordsUtil;
import com.fengx.saltedfish.utils.RandomUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 游戏管理服务
 */
public class GameManageServer {

    private static class Inner {
        private static final GameManageServer INSTANCE = new GameManageServer();
    }

    private GameManageServer() {
    }

    public static GameManageServer getInstance() {
        return GameManageServer.Inner.INSTANCE;
    }

    /**
     * 房间
     * k:房间类型 v:房间列表
     */
    private static final Map<String, List<GameRoomInfo>> ROOM_MAP = new Hashtable<>();

    /**
     * 正在房间的用户
     * k:userId v:roomId
     */
    private static final Map<String, String> ROOM_USER = new Hashtable<>();

    /**
     * k.房间id v.斗地主游戏信息
     */
    private static final Map<String, LandlordsGameInfo> LANDLORDS_MAP = new Hashtable<>();

    /**
     * 检查用户是否已经加入房间了
     *
     * @param userId userId
     * @return roomId
     */
    public String inspectUserJoinRoom(String userId) {
        if (userId == null) {
            return null;
        }
        return ROOM_USER.get(userId);
    }

    /**
     * 获取房间信息
     */
    public GameRoomInfo getRoomInfo(String roomId) {
        if (roomId == null) {
            return null;
        }
        Map<String, GameRoomInfo> roomInfoMap = ROOM_MAP.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toMap(GameRoomInfo::getId, Function.identity()));
        return roomInfoMap.get(roomId);
    }

    /**
     * 获取游戏房间列表
     */
    public List<GameRoomInfo> getRoomList(String type) {
        return ROOM_MAP.get(type);
    }

    /**
     * 添加房间
     */
    public boolean addRoom(RoomTypeEnum typeEnum, GameRoomInfo info) {
        if (CollectionUtils.isEmpty(info.getUserIds())) {
            return false;
        }
        // 检查
        info.getUserIds().forEach(id -> {
            if (inspectUserJoinRoom(id) != null) {
                throw new WarnException("不能重复加入房间！");
            }
        });
        info.setId(RandomUtil.generateShortUuid());
        info.setPlaying(false);
        if (info.getRoomTypeEnum() == RoomTypeEnum.LANDLORDS) {
            info.setNumber(3);
        } else {
            throw new WarnException("房间类型不正确！");
        }
        if (!ROOM_MAP.containsKey(typeEnum.name())) {
            ROOM_MAP.put(typeEnum.name(), Lists.newArrayList());
        }
        ROOM_MAP.get(typeEnum.name()).add(info);
        info.getUserIds().forEach(id -> ROOM_USER.put(id, info.getId()));
        return true;
    }

    /**
     * 退出房间
     */
    public boolean quitRoom(String roomId, String userId) {
        GameRoomInfo gameRoomInfo = getRoomInfo(roomId);
        if (gameRoomInfo != null) {
            if (gameRoomInfo.getPlaying()) {
                return false;
            }
            boolean remove = gameRoomInfo.getUserIds().remove(userId);
            if (remove) {
                ROOM_USER.remove(userId);
                // 通知其他人有人退出房间
                NettyMessage nettyMessage = new NettyMessage();
                nettyMessage.setMsgType(NettyMsgTypeEnum.UPDATE_ROOM_NUMBER);
                nettyMessage.setContent(gameRoomInfo.getUserIds().size() + "");
                NettyHandlerServer.getInstance().sendAllMsgByIds(nettyMessage, gameRoomInfo.getUserIds());
                if (gameRoomInfo.getUserIds().size() == 0) {
                    // 删除房间
                    ROOM_MAP.get(gameRoomInfo.getRoomTypeEnum().name()).remove(gameRoomInfo);
                }
            }
            return remove;
        }
        return true;
    }

    /**
     * 进入房间
     */
    public boolean joinRoom(String roomId, String userId) {
        if (inspectUserJoinRoom(userId) != null) {
            throw new WarnException("不能重复加入房间！");
        }
        GameRoomInfo gameRoomInfo = getRoomInfo(roomId);
        if (gameRoomInfo != null && !gameRoomInfo.getPlaying()) {
            // 重复时也会返回false
            boolean add = gameRoomInfo.getUserIds().add(userId);
            if (add) {
                ROOM_USER.put(userId, roomId);
                NettyMessage nettyMessage = new NettyMessage();
                nettyMessage.setMsgType(NettyMsgTypeEnum.UPDATE_ROOM_NUMBER);
                nettyMessage.setContent(gameRoomInfo.getUserIds().size() + "");
                NettyHandlerServer.getInstance().sendAllMsgByIds(nettyMessage, gameRoomInfo.getUserIds());
                if (gameRoomInfo.getRoomTypeEnum().equals(RoomTypeEnum.LANDLORDS)) {
                    if (gameRoomInfo.getUserIds().size() == 3) {
                        gameRoomInfo.setPlaying(true);
                        // 发牌
                        LandlordsGameInfo gameInfo = new LandlordsGameInfo();
                        List<List<String>> cards = LandlordsUtil.dealCards();
                        HashMap<String, List<String>> hashMap = new HashMap<>(4);
                        HashMap<String, Integer> sortMap = new HashMap<>(3);
                        int i = 0;
                        for (String id : gameRoomInfo.getUserIds()) {
                            hashMap.put(id, cards.get(i));
                            sortMap.put(id, (i + 1));
                            if (i == 0) {
                                gameInfo.setLog(Lists.newArrayList("玩家" + id + "开始叫地主"));
                            }
                            i++;
                        }
                        gameInfo.setAlreadyOutCards(new ArrayList<>());
                        gameInfo.setDipai(cards.get(3));
                        gameInfo.setHandCards(hashMap);
                        gameInfo.setLandlord(null);
                        gameInfo.setSorts(sortMap);
                        LANDLORDS_MAP.put(roomId, gameInfo);
                        // 通知开始游戏
                        NettyMessage begin = new NettyMessage();
                        begin.setMsgType(NettyMsgTypeEnum.BEGIN_GAME);
                        NettyHandlerServer.getInstance().sendAllMsgByIds(begin, gameRoomInfo.getUserIds());
                    }
                }
            }
            return add;
        }
        return false;
    }

    /**
     * 获取游戏相关信息
     */
    public String getGameInfo(GetGameInfoParam param) {
        if (param.getRoomType().equals(RoomTypeEnum.LANDLORDS)) {
            LandlordsGameInfo landlordsGameInfo = LANDLORDS_MAP.get(param.getRoomId());
            if (landlordsGameInfo != null) {
                LandlordsGameInfoVO vo = new LandlordsGameInfoVO();
                List<String> strings = landlordsGameInfo.getHandCards().get(param.getWsUserId());
                vo.setSort(landlordsGameInfo.getSorts().get(param.getWsUserId()));
                vo.setHandCards(strings);
                vo.setLog(landlordsGameInfo.getLog());
                vo.setLandlord(landlordsGameInfo.getLandlord());
                return JsonUtil.object2Json(vo);
            }
        }
        return null;
    }
}
