package com.fengx.saltedfish.model.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 斗地主房间信息
 */
@Data
public class LandlordsGameInfo {

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
     * 已经出的牌
     */
    private List<String> alreadyOutCards;

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
}
