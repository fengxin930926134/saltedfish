package com.fengx.saltedfish.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * json操作工具类
 */
public class JsonUtil {

    /**
     * java对象转换为JSON字符串
     */
    public static <T> String object2Json(T object) {
        return JSON.toJSONString(object);
    }

    /**
     * JSON字符串转换为Java对象
     */
    public static <T> T json2Object(String json, Class<T> obj) {
        JSONObject jsonObject = JSON.parseObject(json);
        return JSON.toJavaObject(jsonObject, obj);
    }

    /**
     * List集合转换为JSON字符串
     */
    public static <T> String list2Json(List<T> list) {
        return JSONArray.toJSONString(list);
    }

    /**
     * 将JSON字符串转换为List集合
     */
    public static <T> List<T> json2List(String json, Class<T> obj) {
        return JSON.parseArray(json, obj);
    }
}
