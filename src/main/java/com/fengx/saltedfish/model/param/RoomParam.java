package com.fengx.saltedfish.model.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RoomParam {
    @NotBlank(message = "id不能为空")
    private String wsUserId;
    @NotBlank(message = "房间id不能为空")
    private String roomId;
}
