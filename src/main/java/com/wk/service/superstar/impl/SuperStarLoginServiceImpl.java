package com.wk.service.superstar.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wk.constant.GlobalConstant;
import com.wk.domain.chaoxing.SuperStarLog;
import com.wk.domain.chaoxing.WkUser;
import com.wk.mapper.SuperStarUserMapper;
import com.wk.service.superstar.SuperStarLogService;
import com.wk.service.superstar.SuperStarLoginService;
import com.wk.service.superstar.SuperStarUserService;
import com.wk.utils.AESCipher;
import com.wk.utils.JsonParser;
import ma.glasnost.orika.MapperFacade;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-登录服务实现类
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
@Service
public class SuperStarLoginServiceImpl extends ServiceImpl<SuperStarUserMapper, WkUser> implements SuperStarLoginService {

    @Autowired
    private AESCipher cipher;

    @Autowired
    private SuperStarLogService superStarLogService;
    
    @Autowired
    private SuperStarUserService userService;
    @Autowired
    private MapperFacade mapperFacade;

    /**
     * 超星登录
     *
     * @param wkUser 用户信息
     */
    @Override
    public void login(WkUser wkUser) {

        WkUser userByAccount = userService.getUserByAccount(wkUser.getAccount());


        SuperStarLog startLog = mapperFacade.map(wkUser, SuperStarLog.class);

        CookieJar cookieJar = new CookieJar() {
            private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<>();
            }
        };

        // 初始化 OkHttpClient 并设置 CookieJar
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();

        String url = "https://passport2.chaoxing.com/fanyalogin";

        FormBody formBody = new FormBody.Builder()
                .add("fid", "-1")
                .add("uname", cipher.encrypt(wkUser.getAccount()))
                .add("password", cipher.encrypt(wkUser.getPassword()))
                .add("refer", "https%3A%2F%2Fi.chaoxing.com")
                .add("t", "true")
                .add("forbidotherlogin", "0")
                .add("validate", "")
                .add("doubleFactorLogin", "0")
                .add("independentId", "0")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            Map<String, Object> result = JsonParser.parse(body); // 解析 JSON 响应
            Map<String, Object> ret = new HashMap<>();

            if (result != null && Boolean.TRUE.equals(result.get("status"))) {
                List<Cookie> cookies = cookieJar.loadForRequest(HttpUrl.parse(url));

                if(userByAccount!=null){
                    userByAccount.setCookies(cookies.toString());
                    this.baseMapper.updateById(userByAccount);
                }else {
                    wkUser.setCookies(cookies.toString());
                    this.save(wkUser);
                }

                startLog.setRemark(GlobalConstant.LOGIN_SUCCESS);

            } else {
                startLog.setRemark(result.get("msg2") != null ? result.get("msg2").toString() : GlobalConstant.LOGIN_FAIL);
            }

        } catch (IOException e) {
            Map<String, Object> ret = new HashMap<>();
            startLog.setRemark(GlobalConstant.LOGIN_FAIL + e.getMessage());
        }
        superStarLogService.saveLog(startLog);
    }
}

