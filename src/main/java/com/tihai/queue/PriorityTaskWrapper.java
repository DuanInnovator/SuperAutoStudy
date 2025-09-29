package com.tihai.queue;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Copyright : DuanInnovator
 * @Description : 优先级任务包装类
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/

@Data
@Slf4j
public class PriorityTaskWrapper implements Runnable {
    private final Runnable realTask;

    private int priority;
    private final String taskId;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public PriorityTaskWrapper(Runnable realTask, int priority, String taskId) {
        this.realTask = realTask;
        this.priority = priority;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        if (cancelled.get()) {
            log.info("任务:{}正在被取消", taskId);
            return;
        }
        realTask.run();
    }


    public boolean cancel() {
        return cancelled.compareAndSet(false, true);
    }


}