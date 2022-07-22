package com.fengx.saltedfish.common.response;


import java.util.Set;

public class SetResponse<T> extends ResponseResult implements Response {


    /**
     * 返回数据
     */
    private Set<T> datas;

    public SetResponse(Integer responseCode, String responseMsg, Set<T> datas) {
        this.status = responseCode;
        this.message = responseMsg;
        this.datas = datas;
    }

    public SetResponse(Set<T> datas) {
        this.status = ResponseEnum.SUCCESS_CODE.getCode();
        this.message = ResponseEnum.SUCCESS_CODE.getMessage();
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
    public Set<T> getDatas() {
        return datas;
    }

    /**
     * @param datas the dateUtils to set
     */
    public void setDatas(Set<T> datas) {
        this.datas = datas;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ListResponse [status=" + status + ", message=" + message + ", dateUtils=" + datas + "]";
    }

}
