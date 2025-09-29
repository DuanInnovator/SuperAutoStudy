package com.tihai.factory;

import com.tihai.queue.PriorityTaskWrapper;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor; // 关键修正：使用JDK标准线程池类

/**
 * 自定义拒绝策略（优先级降级重试）
 */
public class PriorityRejectPolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
            return;
        }

        if (r instanceof PriorityTaskWrapper) {
            PriorityTaskWrapper task = (PriorityTaskWrapper) r;
            if(task.getPriority()>=1){
                task.setPriority(task.getPriority()-1);
            }
            executor.execute(task);   // 重新入队
        }
    }
}