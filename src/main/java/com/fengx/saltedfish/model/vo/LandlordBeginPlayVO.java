package com.fengx.saltedfish.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 开始出牌返回数据
 */
@Data
public class LandlordBeginPlayVO {

    /**
     * 3张底牌
     */
    private List<String> dipai;

    /**
     * 地主id
     */
    private String landlord;

    /**
     * 当前操作的序号
     */
    private Integer currentSort;
}
