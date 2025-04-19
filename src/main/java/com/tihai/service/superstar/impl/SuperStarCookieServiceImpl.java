package com.tihai.service.superstar.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tihai.domain.chaoxing.WkUser;
import com.tihai.mapper.SuperStarCookieMapper;
import com.tihai.service.superstar.SuperStarCookieService;
import org.springframework.stereotype.Service;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-cookie实现
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/26
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@Service
public class SuperStarCookieServiceImpl extends ServiceImpl<SuperStarCookieMapper, WkUser> implements SuperStarCookieService {

    /**
     * 根据登陆账号获取cookie
     * @param loginAccount 登录账号
     */
    @Override
    public String getWkUserCookie(String loginAccount) {
        LambdaQueryWrapper<WkUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WkUser::getAccount, loginAccount);
        wrapper.select(WkUser::getCookies);
        WkUser wkUser = this.getOne(wrapper);
        return wkUser.getCookies();
    }

    /**
     * 更新用户cookie
     * @param wkUser 用户信息
     */
    @Override
    public void updateWkUserCookies(WkUser wkUser) {
        this.updateById(wkUser);
    }
}

