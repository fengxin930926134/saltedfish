package com.fengx.saltedfish.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class LandlordsGameInfoVO {

    private Integer sort;
    /**
     * 游戏日志
     */
    private String log;

    /**
     * 手牌
     */
    private List<String> handCards;

    /**
     * 地主id
     */
    private String landlord;
}
