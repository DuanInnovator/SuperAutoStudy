package com.tihai.service.superstar.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tihai.config.OkHttpClientInit;
import com.tihai.constant.GlobalConstant;
import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.domain.chaoxing.WkUser;
import com.tihai.manager.GlobalCookieManager;
import com.tihai.mapper.SuperStarUserMapper;
import com.tihai.service.superstar.SuperStarLogService;
import com.tihai.service.superstar.SuperStarLoginService;
import com.tihai.utils.AESUtil;
import com.tihai.utils.JsonParser;
import ma.glasnost.orika.MapperFacade;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static com.tihai.common.BaseUrl.API_SSO_LOGIN;
import static com.tihai.common.BaseUrl.LOGIN_URL;

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
    private AESUtil cipher;

    @Autowired
    private SuperStarLogService superStarLogService;

    @Autowired
    private MapperFacade mapperFacade;


    /**
     * 超星登录
     *
     * @param wkUser 用户信息
     */
    @Override
    public CookieJar login(WkUser wkUser) {


        GlobalCookieManager.setCurrentAccount(wkUser.getAccount());
        OkHttpClient client = OkHttpClientInit.getClient(wkUser.getAccount(), false, false);

        SuperStarLog startLog = mapperFacade.map(wkUser, SuperStarLog.class);


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
                .url(LOGIN_URL)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            Map<String, Object> result = JsonParser.parse(body);
            Map<String, Object> ret = new HashMap<>();

            if (result != null && Boolean.TRUE.equals(result.get("status"))) {
                return OkHttpClientInit.getCookieJar(wkUser.getAccount());

            } else {
                startLog.setRemark(result.get("msg2") != null ? result.get("msg2").toString() : GlobalConstant.LOGIN_FAIL);
                return null;
            }

        } catch (IOException e) {
            Map<String, Object> ret = new HashMap<>();
            startLog.setRemark(GlobalConstant.LOGIN_FAIL + e.getMessage());
        }
        superStarLogService.saveLog(startLog);
        return null;
    }


    /**
     * 获取登录信息
     *
     * @return
     */
    @Override
    public Map<String, Object> getLoginInfo(OkHttpClient okHttpClient) {
        try {

            HttpUrl url = HttpUrl.parse(API_SSO_LOGIN).newBuilder().build();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return null;
                }

                String responseBody = response.body().string();
                JSONObject jsonContent = new JSONObject(responseBody);

                // 检查返回结果
                if (jsonContent.optInt("result") == 0) {
                    return null;
                }

                // 提取登录信息
                JSONObject msg = jsonContent.getJSONObject("msg");
                Map<String, Object> loginAcc = new HashMap<>();
                loginAcc.put("puid", msg.getInt("puid"));
                loginAcc.put("name", msg.getString("name"));
                loginAcc.put("sex", msg.getInt("sex"));
                loginAcc.put("phone", msg.getString("phone"));
                loginAcc.put("schoolname", msg.getString("schoolname"));
                loginAcc.put("uname", msg.optString("uname")); // 使用optString处理可能不存在的字段

                return loginAcc;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

