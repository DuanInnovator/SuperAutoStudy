package com.wk.service.superstar.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wk.domain.chaoxing.SuperStarLog;
import com.wk.mapper.SuperStarLogMapper;
import com.wk.service.superstar.SuperStarLogService;
import org.springframework.stereotype.Service;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-日志服务实现类
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Service
public class SuperStarLogServiceImpl extends ServiceImpl<SuperStarLogMapper, SuperStarLog> implements SuperStarLogService {

    /**
     * 保存日志
     * @param superStartLog 日志信息
     */
    @Override
    public void saveLog(SuperStarLog superStartLog) {
        superStartLog.setId(null);
        this.save(superStartLog);
    }

    /**
     * 更新日志
     * @param superStartLog 日志信息
     */
    @Override
    public void updateLog(SuperStarLog superStartLog) {
        this.updateById(superStartLog);
    }

    /**
     * 根据账号和课程名获取最新的一条日志
     * @param account 账号
     * @param courseName 课程
     * @return 最新一条日志
     */
    @Override
    public SuperStarLog getLatestLogByLoginAccount(String account, String courseName) {
        LambdaQueryWrapper<SuperStarLog> superStartLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        superStartLogLambdaQueryWrapper.eq(SuperStarLog::getLoginAccount, account);
        superStartLogLambdaQueryWrapper.eq(SuperStarLog::getCourseName, courseName);
        superStartLogLambdaQueryWrapper.orderByDesc(SuperStarLog::getId).last("limit 1");

        SuperStarLog one = this.getOne(superStartLogLambdaQueryWrapper,false);
        if(one!=null){
            one.setId(null);
        }
        return one;

    }


}

