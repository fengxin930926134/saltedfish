package com.fengx.saltedfish.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 斗地主房间信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LandlordsGameInfo extends BaseGameInfo {

    /**
     * 游戏日志
     */
    private List<String> log;

    /**
     * v:手牌，k.userId
     */
    private Map<String, List<String>> handCards;

    /**
     * k.userId v.sort
     */
    private Map<String, Integer> sorts;

    /**
     * 当前已经出的牌
     */
    private List<String> currentAlreadyOutCards;

    /**
     * 当前已出牌的序号，过牌不算出牌
     */
    private Integer currentOutCardSort;

    /**
     * 3张底牌
     */
    private List<String> dipai;

    /**
     * 地主id
     */
    private String landlord;

    /**
     * 当前序号操作次数，决定操作顺序
     */
    private Map<Integer, Integer> currentNumberMap;

    /**
     * 当前操作的序号
     */
    private Integer currentSort;

    /**
     * 倒计时id
     */
    private String timerId;
}
