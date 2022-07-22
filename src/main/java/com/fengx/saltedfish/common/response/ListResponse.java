package com.fengx.saltedfish.common.response;

import java.util.List;

public class ListResponse<T> extends ResponseResult implements Response {

    /**
     * 返回数据
     */
    private List<T> datas;

    public ListResponse(List<T> datas) {
        this.status = ResponseEnum.SUCCESS_CODE.getCode();
        this.message = ResponseEnum.SUCCESS_CODE.getMessage();
        this.datas = datas;
    }

    public ListResponse(String message, List<T> datas) {
        this.message = message;
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
    public List<T> getDatas() {
        return datas;
    }

    /**
     * @param datas the dateUtils to set
     */
    public void setDatas(List<T> datas) {
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
