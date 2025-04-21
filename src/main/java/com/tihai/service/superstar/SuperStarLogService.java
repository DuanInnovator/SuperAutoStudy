package com.tihai.service.superstar;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tihai.domain.chaoxing.SuperStarLog;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-日志服务接口
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public interface SuperStarLogService extends IService<SuperStarLog> {

    /**
     * 保存日志
     * @param superStartLog 日志信息
     */
    void saveLog(SuperStarLog superStartLog);

    /**
     * 更新日志
     * @param superStartLog 日志信息
     */
    void updateLog(SuperStarLog superStartLog);

    /**
     * 根据账号，课程获取最新一条日志
     * @param account 账号
     * @param courseName 课程
     */
    SuperStarLog getLatestLogByLoginAccount(String account, String courseName);
}

