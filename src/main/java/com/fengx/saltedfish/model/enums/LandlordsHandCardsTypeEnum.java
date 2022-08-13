package com.fengx.saltedfish.model.enums;

import lombok.Getter;

@Getter
public enum LandlordsHandCardsTypeEnum {

    /**
     * 王炸
     */
    KING_BOMB("王炸"),
    /**
     * 炸弹
     */
    BOMB("炸弹"),
    /**
     * 单牌
     */
    SINGLE("单牌"),
    /**
     * 对子
     */
    PAIR("对子"),
    /**
     * 三张牌
     */
    THREE_CARDS("三张牌"),
    /**
     * 三带一
     */
    THREE_WITH_ONE("三带一"),
    /**
     * 三带二
     */
    THREE_BELT_TWO("三带一对"),
    /**
     * 顺子
     */
    STRAIGHT("顺子"),
    /**
     * 连对
     */
    COUPLE("连对"),
    /**
     * 三顺
     */
    SANSHUN("三顺"),
    /**
     * 飞机
     */
    AIRCRAFT("飞机"),
    /**
     * 错误
     */
    ERROR("错误");

    private final String desc;

    LandlordsHandCardsTypeEnum(String desc) {
        this.desc = desc;
    }
}
