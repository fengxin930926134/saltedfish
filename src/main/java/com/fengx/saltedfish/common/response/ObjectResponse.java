package com.fengx.saltedfish.common.response;

public class ObjectResponse<T> extends ResponseResult implements Response {

    /**
     * 返回数据
     */
    private T datas;

    public ObjectResponse() {
        super();
    }

    public ObjectResponse(T datas) {
        this.status = ResponseEnum.SUCCESS_CODE.getCode();
        this.message = ResponseEnum.SUCCESS_CODE.getMessage();
        this.datas = datas;
    }

    public ObjectResponse(String message, T datas) {
        this.status = ResponseEnum.SUCCESS_CODE.getCode();
        this.message = message;
        this.datas = datas;
    }

    public ObjectResponse(Integer responseCode, String responseMsg, T datas) {
        this.status = responseCode;
        this.message = responseMsg;
        this.datas = datas;
    }

    @Override
    public Integer getStatus() {
        return status;
    }


    @Override
    public String getMessage() {
        return message;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the dateUtils
     */
    public T getDatas() {
        return datas;
    }

    /**
     * @param datas the dateUtils to set
     */
    public void setDatas(T datas) {
        this.datas = datas;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ObjectResponse [status=" + status + ", message=" + message + ", dateUtils=" + datas + "]";
    }

}
