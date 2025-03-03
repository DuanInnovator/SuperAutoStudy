package com.wk.service.superstar;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wk.domain.chaoxing.WkUser;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-登录服务接口
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
public interface SuperStarLoginService extends IService<WkUser> {

    /**
     * 超星登录
     * @param wkUser 用户信息
     */
    void login(WkUser wkUser);
}

