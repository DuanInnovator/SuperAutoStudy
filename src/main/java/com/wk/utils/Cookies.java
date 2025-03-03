package com.wk.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wk.config.GlobalConst;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description :
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class Cookies {
    private static final String COOKIES_PATH = GlobalConst.COOKIES_PATH;
    private static final Gson gson = new Gson();

    // 中间JSON对象
    private static class CookieData {
        String name;
        String value;
        long expiresAt;
        String domain;
        String path;
        boolean secure;
        boolean httpOnly;
        boolean persistent;
        boolean hostOnly;

        static CookieData fromCookie(Cookie cookie) {
            CookieData data = new CookieData();
            data.name = cookie.name();
            data.value = cookie.value();
            data.expiresAt = cookie.expiresAt();
            data.domain = cookie.domain();
            data.path = cookie.path();
            data.secure = cookie.secure();
            data.httpOnly = cookie.httpOnly();
            data.persistent = cookie.persistent();
            data.hostOnly = cookie.hostOnly();
            return data;
        }

        Cookie toCookie() {
            return new Cookie.Builder()
                    .name(name)
                    .value(value)
                    .expiresAt(expiresAt)
                    .domain(domain)
                    .path(path)
//                    .secure(secure)
//                    .httpOnly(httpOnly)
//                    .hostOnly(hostOnly)
                    .build();
        }
    }



    /**
     * 从JSON文件加载Cookie
     */
    public static List<Cookie> useCookies() {
        File file = new File(COOKIES_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(COOKIES_PATH)) {
            List<CookieData> dataList = gson.fromJson(reader,
                    new TypeToken<List<CookieData>>(){}.getType());

            List<Cookie> cookies = new ArrayList<>();
            for (CookieData data : dataList) {
                cookies.add(data.toCookie());
            }
            return cookies;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
}

