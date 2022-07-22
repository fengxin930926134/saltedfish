package com.fengx.saltedfish.interceptor;

import com.fengx.saltedfish.common.RequestHolder;
import com.fengx.saltedfish.utils.DateUtil;
import com.fengx.saltedfish.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方法拦截器
 * 功能：
 * 1.方法效率计时
 */
@Slf4j
public class HttpInterceptor implements HandlerInterceptor {

    /**
     * 请求过来之前处理
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String url = request.getRequestURL().toString();
        Map<String, String[]> parameterMap = request.getParameterMap();
        // post请求参数getReader()只能读取一次 所以不管post的参数
        log.info("{} {}: {}, params: {}", Thread.currentThread().getName(), request.getMethod(), url, JsonUtil.object2Json(parameterMap));
        // 把request请求放入其中
        RequestHolder.add(request);
        DateUtil.startTimer();
        return true;
    }

    /**
     * 请求正常结束之后调用
     */
    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        removeThreadLocalInfo();
    }

    /**
     * 请求正常结束之后调用
     * 异常情况下也会调用
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        String url = request.getRequestURL().toString();
        log.info("{} {}: {}, time elapsed: {} ms", Thread.currentThread().getName(), request.getMethod(), url, DateUtil.endTimer());
        removeThreadLocalInfo();
    }

    /**
     * 移除本地线程信息
     */
    private void removeThreadLocalInfo() {
        RequestHolder.remove();
    }
}
