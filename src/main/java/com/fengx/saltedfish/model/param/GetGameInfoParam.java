package com.fengx.saltedfish.model.param;

import com.fengx.saltedfish.model.enums.RoomTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class GetGameInfoParam {
    @NotBlank(message = "id不能为空")
    private String wsUserId;
    @NotBlank(message = "房间id不能为空")
    private String roomId;
    @NotNull
    private RoomTypeEnum roomType;
}
