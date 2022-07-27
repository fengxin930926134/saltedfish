package com.fengx.saltedfish.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 叫地主参数
 */
@Data
public class BeLandlordParam {
    @NotBlank(message = "Id不能为空")
    private String userId;
    @NotNull(message = "是否不能为空")
    private Boolean isBeLandlord;
}
