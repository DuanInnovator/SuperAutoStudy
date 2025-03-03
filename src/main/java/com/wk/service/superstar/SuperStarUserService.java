package com.wk.service.superstar;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wk.domain.chaoxing.WkUser;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-用户信息
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public interface SuperStarUserService extends IService<WkUser> {

    /**
     * 根据账号查询用户信息
     * @param account 账号
     */
    WkUser getUserByAccount(String account);
}

