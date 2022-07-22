package com.fengx.saltedfish.common.response;

import lombok.Getter;

@Getter
public enum ResponseEnum {
    SUCCESS_CODE(200, "操作成功!"),
    NO_LOGIN_CODE(401, "您未登录!"),
    AGAIN_TOKEN_CODE(402, "重新生成票据!"),
    NO_PRESSION_CODE(403, "您无权限访问!"),
    NO_SOURCES_CODE(404, "资源不存在!"),
    WARN_CODE(405, "警告"),
    FAIL_CODE(500, "操作失败!");
    private Integer code;

    private String message;

    ResponseEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
