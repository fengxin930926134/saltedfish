package com.fengx.saltedfish.controller;

import com.fengx.saltedfish.common.response.Response;
import com.fengx.saltedfish.model.param.BeLandlordParam;
import com.fengx.saltedfish.model.param.CreateRoomParam;
import com.fengx.saltedfish.model.param.GetGameInfoParam;
import com.fengx.saltedfish.model.param.RoomParam;
import com.fengx.saltedfish.service.NettyGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/netty/")
@RequiredArgsConstructor
public class NettyGameController {

    private final NettyGameService nettyGameService;

    @GetMapping(value = "/roomList/{roomTypeEnum}")
    public Response roomList(@PathVariable String roomTypeEnum) {
        return nettyGameService.roomList(roomTypeEnum);
    }

    @PostMapping(value = "/createRoom")
    public Response createRoom(@Valid @RequestBody CreateRoomParam param) {
        return nettyGameService.createRoom(param);
    }

    @PostMapping(value = "/quitRoom")
    public Response quitRoom(@Valid @RequestBody RoomParam param) {
        return nettyGameService.quitRoom(param);
    }

    @PostMapping(value = "/joinRoom")
    public Response joinRoom(@Valid @RequestBody RoomParam param) {
        return nettyGameService.joinRoom(param);
    }

    @GetMapping(value = "/inspectJoinRoom/{userId}")
    public Response inspectJoinRoom(@PathVariable String userId) {
        return nettyGameService.inspectJoinRoom(userId);
    }

    @PostMapping(value = "/getGameInfo")
    public Response getGameInfo(@Valid @RequestBody GetGameInfoParam param) {
        return nettyGameService.getGameInfo(param);
    }

    @PostMapping(value = "/beLandlord")
    public Response beLandlord(@Valid @RequestBody BeLandlordParam param) {
        return nettyGameService.beLandlord(param);
    }
}
