package com.fengx.saltedfish.common.response;

public class SuccessResponse extends ResponseResult implements Response {

    /**
     * 返回消息
     */
    private String message;

    public SuccessResponse() {
        this.message = ResponseEnum.SUCCESS_CODE.getMessage();
    }

    public SuccessResponse(String message) {
        this.message = message;
    }


    /**
     * 返回结果编码
     *
     * @return
     */
    @Override
    public Integer getStatus() {
        return ResponseEnum.SUCCESS_CODE.getCode();
    }

    /**
     * 返回结果信息
     *
     * @return
     */
    @Override
    public String getMessage() {
        return this.message;
    }
}
