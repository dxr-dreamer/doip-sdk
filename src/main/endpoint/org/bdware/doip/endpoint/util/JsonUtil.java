package org.bdware.doip.endpoint.util;

import com.google.gson.Gson;

public class JsonUtil {
    private static final Gson gson = new Gson();  // 创建一个Gson实例

    // 将任意对象转换为JSON字符串
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    // 将JSON字符串转换为指定类型的对象
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
}