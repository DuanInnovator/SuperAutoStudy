package com.tihai.service.superstar.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.domain.chaoxing.SuperStarTask;
import com.tihai.enums.WkTaskStatusEnum;
import com.tihai.mapper.SuperStarLogMapper;
import com.tihai.service.superstar.SuperStarLogService;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-日志服务实现类
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Service
@SuppressWarnings("all")
public class SuperStarLogServiceImpl extends ServiceImpl<SuperStarLogMapper, SuperStarLog> implements SuperStarLogService {

    @Autowired
    private MapperFacade mapperFacade;


    /**
     * 创建新的日志
     *
     * @param task 任务
     * @return 日志
     */
    public SuperStarLog createNewLog(SuperStarTask task) {
        SuperStarLog log = mapperFacade.map(task, SuperStarLog.class);
        log.setStartTime(LocalDateTime.now());
        log.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
        log.setId(IdWorker.getId());
        return log;
    }


    /**
     * 保存日志
     *
     * @param superStartLog 日志信息
     */
    @Override
    public void saveLog(SuperStarLog superStartLog) {
        superStartLog.setId(null);
        this.save(superStartLog);
    }

    /**
     * 批量保存日志
     *
     * @param superStartLog 日志信息
     */
    @Override
    public void batchSaveLog(List<SuperStarLog> superStartLog) {
        this.saveOrUpdateBatch(superStartLog);
    }


    /**
     * 根据账号，课程获取最新一条日志
     *
     * @param account    账号
     * @param courseName 课程名称
     * @param courseId   课程ID
     */
    @Override
    public SuperStarLog getLatestLogByLoginAccount(String account, String courseName, String courseId) {
        LambdaQueryWrapper<SuperStarLog> superStartLogLambdaQueryWrapper = new LambdaQueryWrapper<>();
        superStartLogLambdaQueryWrapper.eq(SuperStarLog::getLoginAccount, account);
        if (courseId != null) {
            superStartLogLambdaQueryWrapper.eq(SuperStarLog::getCourseId, courseId);
        } else if (courseName != null) {
            superStartLogLambdaQueryWrapper.eq(SuperStarLog::getCourseName, courseName);
        }
        superStartLogLambdaQueryWrapper.eq(SuperStarLog::getIsDelete, 0);
        superStartLogLambdaQueryWrapper.orderByDesc(SuperStarLog::getEndTime)
                .last("limit 1");

        return this.getOne(superStartLogLambdaQueryWrapper, false);

    }


}

