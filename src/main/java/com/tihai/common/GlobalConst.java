package com.tihai.common;

import java.util.Collections;
import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description :
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class GlobalConst {
    // AES 加密密钥
    public static final String AES_KEY = "u2oh6Vu^HWe4_AES";

    // Cookies 存储路径
    public static final String COOKIES_PATH = "cookies.txt";

    // 操作间隔阈值（秒）
    public static final int THRESHOLD = 3;

    // 通用请求头
    public static final Map<String, String> HEADERS = createImmutableMap(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
            "Sec-Ch-Ua", "\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\""
    );

    // 视频相关请求头
    public static final Map<String, String> VIDEO_HEADERS = createImmutableMap(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
            "Referer", "https://mooc1.chaoxing.com/ananas/modules/video/index.html?v=2023-1110-1610",
            "Host", "mooc1.chaoxing.com"
    );

    // 音频相关请求头
    public static final Map<String, String> AUDIO_HEADERS = createImmutableMap(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
            "Referer", "https://mooc1.chaoxing.com/ananas/modules/audio/index_new.html?v=2023-0428-1705",
            "Host", "mooc1.chaoxing.com"
    );

    // 创建不可修改的 Map
    private static Map<String, String> createImmutableMap(String... keyValues) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    // 禁止实例化
    private GlobalConst() {
        throw new AssertionError("Cannot instantiate constant class");
    }
}

