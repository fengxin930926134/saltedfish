package com.fengx.saltedfish.utils;

import org.springframework.beans.BeanUtils;

public class CopyUtil {
    /**
     * 对象属性的拷贝
     *
     * @param source 源对象
     * @param target 目标对象
     * @return V 目标对象
     */
    public static <T, V> V beanCopy(T source, V target) {
        if (source == null || target == null) {
            return null;
        }
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
