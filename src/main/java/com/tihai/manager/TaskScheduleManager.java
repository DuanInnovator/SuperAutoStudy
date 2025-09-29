package com.tihai.manager;

import com.tihai.service.superstar.impl.SuperStarTaskServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Copyright : DuanInnovator
 * @Description : 线程任务管理
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/27
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Service
public class TaskScheduleManager {
    @Autowired
    private SuperStarTaskServiceImpl taskService;

    private volatile boolean isNightMode = false;

//    @PostConstruct
//    public void init() throws NacosException {
//        checkTimeAndAdjust();
//    }

    // 每分钟检查一次时间
//    @Scheduled(cron = "0 * * * * ?")
//    public void checkTimeAndAdjust() throws NacosException {
//        LocalTime now = LocalTime.now();
//        LocalTime nightStart = LocalTime.of(10,32 ); // 23:00
//        LocalTime morningStart = LocalTime.of(5, 0);  // 05:00
//
//        if (now.isAfter(nightStart) || now.isBefore(morningStart)) {
//
//                isNightMode = true;
//                taskService.shutdownThreadPoolGracefully();
//
//        } else {
//
//                // 进入日间模式
//                isNightMode = false;
//                taskService.restartThreadPoolAndTasks();
//
//        }
//    }
}