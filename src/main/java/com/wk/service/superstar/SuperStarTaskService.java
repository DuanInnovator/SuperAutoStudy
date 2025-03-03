package com.wk.service.superstar;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wk.domain.chaoxing.SuperStarTask;
import com.wk.dto.CourseSubmitTaskDTO;

import java.io.IOException;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通=服务接口
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
public interface SuperStarTaskService extends IService<SuperStarTask> {
    /**
     * 添加超星学习任务
     * @param courseSubmitTaskDTO
     */
     void addChaoXingTask(CourseSubmitTaskDTO courseSubmitTaskDTO);


    /**
     * 从任务队列中取出未开始的超星学习任务，开始执行
     */
    void startChaoxingTask();

    /**
     * 执行超星学习任务
     * @param task
     */
    void executeCourseTask(SuperStarTask task) throws IOException;
}

