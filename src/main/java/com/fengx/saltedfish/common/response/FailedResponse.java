package com.fengx.saltedfish.common.response;

public class FailedResponse<T> extends ResponseResult implements Response {
    public FailedResponse() {
        this.status = ResponseEnum.FAIL_CODE.getCode();
        this.message = ResponseEnum.FAIL_CODE.getMessage();
    }

    public FailedResponse(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public FailedResponse(String message) {
        this.status = ResponseEnum.FAIL_CODE.getCode();
        this.message = message;
    }


    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
