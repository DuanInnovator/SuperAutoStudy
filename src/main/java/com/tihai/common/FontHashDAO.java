package com.tihai.common;

import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description : 字体哈希映射
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
public class FontHashDAO {
    private Map<String, String> charMap;
    private Map<String, String> hashMap;

    public FontHashDAO(String filePath) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(filePath))) {
            Gson gson = new Gson();
            charMap = gson.fromJson(reader, Map.class);
            hashMap = new HashMap<>();
            for (Map.Entry<String, String> entry : charMap.entrySet()) {
                hashMap.put(entry.getValue(), entry.getKey());
            }
        }
    }

    public String findChar(String fontHash) {
        return hashMap.get(fontHash);
    }

    public String findHash(String character) {
        return charMap.get(character);
    }
}