package com.fengx.saltedfish.server;

import com.fengx.saltedfish.common.exception.WarnException;
import com.fengx.saltedfish.model.entity.GameRoomInfo;
import com.fengx.saltedfish.model.entity.LandlordsGameInfo;
import com.fengx.saltedfish.model.entity.NettyMessage;
import com.fengx.saltedfish.model.enums.NettyMsgTypeEnum;
import com.fengx.saltedfish.model.enums.RoomTypeEnum;
import com.fengx.saltedfish.model.param.GetGameInfoParam;
import com.fengx.saltedfish.model.vo.LandlordBeginPlayVO;
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
     * 叫地主时临时使用, k.roomId v.下标序号，值权重，叫抢一次+1
     *
     * 当某位玩家叫完地主后，按照次序每位玩家均有且只有一次“抢地主”的机会。玩家选择“抢地主”后，如果没有其他玩家继续“抢地主”则地主权利属于该名“抢地主”的玩家。
     * 如果没有任何玩家选择“抢地主”，则地主权利属于“叫地主”的玩家。
     * 凡是有过“不叫地主”操作的玩家无法进行“抢地主”的操作
     */
    private static final Map<String, Integer[]> BE_LANDLORDS_MAP = new Hashtable<>();

    /**
     * 切换下一位需要执行的操作
     */
    @FunctionalInterface
    public interface Operation {

        void run(Integer nextSort);
    }

    /**
     * 切换下一位操作
     */
    public void nextOperation(Map<Integer, Integer> currentNumberMap, Collection<String> ids, Operation operation) {
        Integer currentSort = getCurrentSort(currentNumberMap);
        Integer integer = currentNumberMap.get(currentSort);
        currentNumberMap.put(currentSort, ++integer);
        // 通知
        NettyMessage message = new NettyMessage();
        Integer sort = getCurrentSort(currentNumberMap);
        message.setContent(sort.toString());
        message.setMsgType(NettyMsgTypeEnum.NEXT_OPERATION);
        NettyHandlerServer.getInstance().sendAllMsgByIds(message, ids);
        if (operation != null) {
            operation.run(sort);
        }
    }

    /**
     * 推送日志
     *
     * @param content 内容
     */
    public void pushLog(String content, Collection<String> ids) {
        NettyMessage message = new NettyMessage();
        message.setContent(content);
        message.setMsgType(NettyMsgTypeEnum.PUSH_LOG);
        NettyHandlerServer.getInstance().sendAllMsgByIds(message, ids);
    }

    /**
     * 游戏结束
     */
    public void gameOver(String roomId, Collection<String> ids) {
        // 退出房间
        LANDLORDS_MAP.remove(roomId);
        ids.forEach(ROOM_USER::remove);
        ROOM_MAP.forEach((k, v) -> v.remove(getRoomInfo(roomId)));
        // 结束消息
        NettyMessage message = new NettyMessage();
        message.setMsgType(NettyMsgTypeEnum.GAME_OVER);
        NettyHandlerServer.getInstance().sendAllMsgByIds(message, ids);
    }

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
     * 获取斗地主的正在游戏的房间相关信息
     */
    public LandlordsGameInfo getLandlordsGameInfo(String roomId) {
        LandlordsGameInfo landlordsGameInfo = LANDLORDS_MAP.get(roomId);
        landlordsGameInfo.setCurrentSort(getCurrentSort(landlordsGameInfo.getCurrentNumberMap()));
        return landlordsGameInfo;
    }

    /**
     * 获取叫地主的临时权重数组
     */
    public Integer[] getBeLandlordsMap(String roomId) {
        return BE_LANDLORDS_MAP.get(roomId);
    }

    /**
     * 设置一系列相关还需要设置的内容，然后通知可以开始出牌了
     */
    public void landlordBeginPlay(String roomId) {
        // 去掉临时权重
        BE_LANDLORDS_MAP.remove(roomId);
        LandlordsGameInfo landlordsGameInfo = LANDLORDS_MAP.get(roomId);
        Set<String> userIds = landlordsGameInfo.getHandCards().keySet();
        // 通知地主是 通知底牌
        String collect = String.join("、", landlordsGameInfo.getDipai());
        String log = "地主是玩家" + landlordsGameInfo.getLandlord() + "\n底牌是 " + collect;
        landlordsGameInfo.getLog().add(log);
        pushLog(log, userIds);
        // 更新出牌顺序，把底牌加入该玩家手里
        Integer sort = landlordsGameInfo.getSorts().get(landlordsGameInfo.getLandlord());
        setCurrentSort(landlordsGameInfo.getCurrentNumberMap(), sort);
        landlordsGameInfo.getHandCards().get(landlordsGameInfo.getLandlord()).addAll(landlordsGameInfo.getDipai());
        landlordsGameInfo.getHandCards().get(landlordsGameInfo.getLandlord()).sort(LandlordsUtil.comparator);
        // 发送出牌开始通知以及携带相关信息，把底牌加入玩家手里
        NettyMessage message = new NettyMessage();
        LandlordBeginPlayVO vo = new LandlordBeginPlayVO();
        vo.setCurrentSort(sort);
        vo.setDipai(landlordsGameInfo.getDipai());
        vo.setLandlord(landlordsGameInfo.getLandlord());
        message.setContent(JsonUtil.object2Json(vo));
        message.setMsgType(NettyMsgTypeEnum.LANDLORD_BEGIN_PLAY);
        NettyHandlerServer.getInstance().sendAllMsgByIds(message, userIds);
    }

    /**
     * 重新开局
     */
    public void landlordRefreshGame(String roomId, Set<String> userIds) {
        // 发牌
        landlordsDealCards(roomId, userIds);
        // 通知开始游戏
        NettyMessage begin = new NettyMessage();
        begin.setMsgType(NettyMsgTypeEnum.BEGIN_GAME);
        NettyHandlerServer.getInstance().sendAllMsgByIds(begin, userIds);
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
                        landlordRefreshGame(roomId, gameRoomInfo.getUserIds());
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
                vo.setCurrentSort(getCurrentSort(landlordsGameInfo.getCurrentNumberMap()));
                return JsonUtil.object2Json(vo);
            }
        }
        return null;
    }

    /**
     * 斗地主发牌
     */
    private void landlordsDealCards(String roomId, Set<String> userIds) {
        // 发牌
        LandlordsGameInfo gameInfo = new LandlordsGameInfo();
        List<List<String>> cards = LandlordsUtil.dealCards();
        HashMap<String, List<String>> hashMap = new HashMap<>(4);
        HashMap<String, Integer> sortMap = new HashMap<>(3);
        HashMap<Integer, Integer> currentBeLandlord = new HashMap<>(3);
        int i = 0;
        for (String id : userIds) {
            hashMap.put(id, cards.get(i));
            sortMap.put(id, (i + 1));
            currentBeLandlord.put((i + 1), 0);
            if (i == 0) {
                gameInfo.setLog(Lists.newArrayList("玩家" + id + "开始操作"));
            }
            i++;
        }
        gameInfo.setDipai(cards.get(3));
        gameInfo.setHandCards(hashMap);
        gameInfo.setLandlord(null);
        gameInfo.setSorts(sortMap);
        gameInfo.setCurrentNumberMap(currentBeLandlord);
        gameInfo.setCurrentSort(1);
        LANDLORDS_MAP.put(roomId, gameInfo);
        BE_LANDLORDS_MAP.put(roomId, new Integer[]{0, 0, 0});
    }

    /**
     * 获取当前操作的序号
     */
    private Integer getCurrentSort(Map<Integer, Integer> currentNumberMap) {
        if (currentNumberMap != null) {
            final Integer[] csort = {null};
            final Integer[] cnumber = {null};
            for (int i = 1; i <= 3; i++) {
                Integer number = currentNumberMap.get(i);
                if (number != null) {
                    if (i == 1) {
                        csort[0] = i;
                        cnumber[0] = number;
                    } else {
                        if (cnumber[0] > number) {
                            csort[0] = i;
                            break;
                        }
                    }
                }
            }
            return csort[0];
        }
        return 1;
    }

    /**
     * 设置当前操作顺序
     */
    private void setCurrentSort(Map<Integer, Integer> currentNumberMap, Integer sort) {
        if (currentNumberMap != null && sort != null) {
            boolean is = false;
            for (int i = 1; i <= 3; i++) {
                Integer number = currentNumberMap.get(i);
                if (number != null) {
                    if (i == sort) {
                        currentNumberMap.put(i, 0);
                        is = true;
                    } else {
                        if (is) {
                            currentNumberMap.put(i, 0);
                        } else {
                            currentNumberMap.put(i, 1);
                        }
                    }
                }
            }
        }
    }
}
