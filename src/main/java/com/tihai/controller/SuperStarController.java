package com.tihai.controller;

import com.tihai.common.R;
import com.tihai.dubbo.dto.CourseSubmitTaskDTO;
import com.tihai.enums.BizCodeEnum;
import com.tihai.service.superstar.impl.SuperStarTaskServiceImpl;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

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
    public R addChaxingTask(@RequestBody @Valid CourseSubmitTaskDTO courseSubmitTaskDTO) {

        try {
            chaoXingTaskService.addChaoXingTask(courseSubmitTaskDTO);
        } catch (Exception e) {

            return R.error(BizCodeEnum.TASK_ALREADY_EXIST.getCode(), BizCodeEnum.TASK_ALREADY_EXIST.getMsg());
        }
        return R.ok();
    }

    /**
     * 启动任务
     */
    @GetMapping("")
    public void start(){
        chaoXingTaskService.startChaoxingTask();
    }
}

