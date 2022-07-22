package com.fengx.saltedfish.common.response;

public interface Response {
    /**
     * 返回结果编码
     *
     * @return
     */
    Integer getStatus();

    /**
     * 返回结果信息
     *
     * @return
     */
    String getMessage();
}
