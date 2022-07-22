package com.fengx.saltedfish.common;

import javax.servlet.http.HttpServletRequest;

/**
 * 存储本地HTTP请求，当前登录用户
 */
public class RequestHolder {

    private static final ThreadLocal<HttpServletRequest> REQUEST_HOLDER = new ThreadLocal<>();

    public static void add(HttpServletRequest request) {
        REQUEST_HOLDER.set(request);
    }

    public static HttpServletRequest currentRequest() {
        return REQUEST_HOLDER.get();
    }

    public static void remove() {
        REQUEST_HOLDER.remove();
    }
}
