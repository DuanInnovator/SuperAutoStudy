package com.tihai.service.superstar;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.domain.chaoxing.SuperStarTask;
import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-日志服务接口
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public interface SuperStarLogService extends IService<SuperStarLog> {


    /**
     * 创建新的日志
     *
     * @param task 任务
     * @return 日志
     */
    SuperStarLog createNewLog(SuperStarTask task);


    /**
     * 保存日志
     *
     * @param superStartLog 日志信息
     */
    void saveLog(SuperStarLog superStartLog);

    /**
     * 批量保存日志
     *
     * @param superStartLog 日志信息
     */
    void batchSaveLog(List<SuperStarLog> superStartLog);

    /**
     * 根据账号，课程获取最新一条日志
     *
     * @param account    账号
     * @param courseName 课程名称
     * @param courseId   课程ID
     */
    SuperStarLog getLatestLogByLoginAccount(String account, String courseName, String courseId);
}

