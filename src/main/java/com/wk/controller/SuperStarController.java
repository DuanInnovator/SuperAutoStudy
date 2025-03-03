package com.wk.controller;

import com.wk.domain.chaoxing.SuperStarTask;
import com.wk.dto.CourseSubmitTaskDTO;
import com.wk.service.superstar.impl.SuperStarTaskServiceImpl;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-控制器
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/28
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
@RestController
@RequestMapping("/chaoxing")
public class SuperStarController {

    @Autowired
    private SuperStarTaskServiceImpl chaoXingTaskService;

    @Autowired
    private MapperFacade mapperFacade;

    /**
     * 添加网课代刷任务
     *
     * @return
     */
    @PostMapping("")
    public String addChaxingTask(@RequestBody CourseSubmitTaskDTO courseSubmitTaskDTO) {
//            chaoXingTaskService.startChaoxingTask();

        chaoXingTaskService.executeCourseTask(mapperFacade.map(courseSubmitTaskDTO, SuperStarTask.class));
        return "success";
    }
}

