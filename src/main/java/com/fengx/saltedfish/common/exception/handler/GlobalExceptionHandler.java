package com.fengx.saltedfish.common.exception.handler;

import com.fengx.saltedfish.common.exception.WarnException;
import com.fengx.saltedfish.common.response.FailedResponse;
import com.fengx.saltedfish.common.response.Response;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获警告
     */
    @ExceptionHandler(WarnException.class)
    public Response warnException(WarnException e) {
        e.printStackTrace();
        return new FailedResponse<>(e.getMessage());
    }

    /**
     * 未使用转换实体注解
     */
    @ExceptionHandler(BindException.class)
    public Response bindException() {
        log.error("未使用注解 @RequestBody 转换实体导致实体转换失败");
        return new FailedResponse<>("转换实体失败");
    }

    /**
     * 参数合法性校验异常
     * p:通常是参数和实体匹配不上导致json转换失败
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response validationException(HttpMessageNotReadableException e) {
        e.printStackTrace();
        return new FailedResponse<>("参数不合法");
    }

    /**
     * 参数合法性校验异常
     * p:有参校验
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response validationException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        if (result.hasErrors()) {
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error: errors) {
                return new FailedResponse<>(error.getDefaultMessage());
            }
        }
        return new FailedResponse<>("参数异常");
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Response nullPointerExceptionHandler(NullPointerException e) {
        e.printStackTrace();
        return new FailedResponse<>("空指针异常");
    }


    /**
     * 其它异常
     */
    @ExceptionHandler(Exception.class)
    public Response exceptionHandler(Exception e) {
        e.printStackTrace();
        return new FailedResponse<>(e.getMessage() != null? e.getMessage(): "发生未知异常");
    }

}