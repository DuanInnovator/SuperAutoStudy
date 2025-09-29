package com.tihai.service.dubbo.task;

/**
 * @Copyright : DuanInnovator
 * @Description 超星学习通任务Service
 * @Author : DuanInnovator
 * @CreateTime : 2025/5/20
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public interface SuperStarTaskService {

    /**
     * 暂停任务
     * @param orderId 订单id
     */
    void pauseTaskByOrderId(String orderId);

    /**
     * 补刷任务
     */
    void touchUpTaskByOrderId(String orderId);
}

