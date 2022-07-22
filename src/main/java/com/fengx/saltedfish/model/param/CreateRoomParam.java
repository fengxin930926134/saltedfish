package com.fengx.saltedfish.model.param;

import com.fengx.saltedfish.model.RoomTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateRoomParam {

    @NotBlank(message = "id不能为空")
    private String wsUserId;
    @NotBlank(message = "房间名不能为空")
    private String roomName;
    /**
     * 房间类型
     */
    @NotNull(message = "房间类型不能为空")
    private RoomTypeEnum roomTypeEnum;
}
