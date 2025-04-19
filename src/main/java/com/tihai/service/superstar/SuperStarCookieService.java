package com.tihai.service.superstar;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tihai.domain.chaoxing.WkUser;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-cookie接口
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/26
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
public interface SuperStarCookieService extends IService<WkUser> {

    /**
     * 根据账号获取cookie
     * @param loginAccount 登录账号
     */
    String getWkUserCookie(String loginAccount);

    /**
     * 更新cookie
     * @param wkUser 用户信息
     */
    void updateWkUserCookies(WkUser wkUser);
}

