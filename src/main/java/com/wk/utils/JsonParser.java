package com.wk.utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description :
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/27
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class JsonParser {

    /**
     * 将 JSON 字符串解析为 Map<String, Object>
     *
     * @param jsonString 待解析的 JSON 字符串
     * @return 解析后的 Map
     */
    public static Map<String, Object> parse(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        return toMap(jsonObject);
    }

    /**
     * 将 JSONObject 转换为 Map
     *
     * @param jsonObject 待转换的 JSONObject
     * @return 转换后的 Map
     */
    private static Map<String, Object> toMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keysItr = jsonObject.keys();
        while(keysItr.hasNext()){
            String key = keysItr.next();
            Object value = jsonObject.get(key);
            if(value instanceof JSONObject){
                value = toMap((JSONObject)value);
            }
            map.put(key, value);
        }
        return map;
    }
}

