package com.fengx.saltedfish.model.param;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PlayBrandParam {
    @NotNull
    private Boolean play;
    private List<String> brand;
    private String userId;
}
