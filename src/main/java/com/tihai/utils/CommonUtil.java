package com.tihai.utils;

import com.tihai.manager.GlobalCookieManager;
import okhttp3.Cookie;

import java.security.MessageDigest;
import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description : 公共工具类
 * @Author : DuanInnovator
 * @CreateTime : 2025/9/21
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
public class CommonUtil {

    /**
     * 从当前用户的Cookie信息获取对应的key
     *
     * @param key key
     * @return value
     */
    public static String getValue(String key) {
        String account = GlobalCookieManager.getInstance().getCurrentAccount();
        List<Cookie> cookies = GlobalCookieManager.getAccountCookies(account);
        return cookies.stream().filter(cookie -> cookie.name().equals(key)).findFirst().get().value();
    }


    /**
     * 生成 MD5 加密串
     */
    public static String getEnc(String clazzId, String jobId, String objectId, long playingTime, long duration, String userId)
            throws Exception {
        String str = String.format("[%s][%s][%s][%s][%d][d_yHJ!$pdA~5][%d][0_%s]",
                clazzId, userId, jobId, objectId, playingTime * 1000, duration * 1000, duration);
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

//    /**
//     * 获取随机秒数
//     *
//     * @return 随机秒数
//     */
//    public static int getRandomSeconds() {
//        return 60;
//    }

    /**
     * 获取当前时间戳
     *
     * @return 时间戳字符串
     */
    public static String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
}

