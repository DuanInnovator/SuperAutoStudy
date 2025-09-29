package com.tihai.manager;

import com.tihai.queue.PriorityTaskWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Copyright : DuanInnovator
 * @Description : 任务管理器
 * @Author : DuanInnovator
 * @CreateTime : 2025/9/24
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
@Slf4j
public class TaskManager {

    private final ConcurrentMap<String, Future<?>> taskFutureMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, PriorityTaskWrapper> taskWrapperMap = new ConcurrentHashMap<>();

    @Autowired
    private ThreadPoolExecutor taskExecutor;


    /**
     * 提交任务
     */
    public void submitTask(PriorityTaskWrapper taskWrapper) {
        if (taskWrapperMap.containsKey(taskWrapper.getTaskId())) {
            log.warn("Task {} already exists", taskWrapper.getTaskId());
            return;
        }

        Future<?> future = taskExecutor.submit(taskWrapper);

        taskWrapperMap.put(taskWrapper.getTaskId(), taskWrapper);
        taskFutureMap.put(taskWrapper.getTaskId(), future);

    }


    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        PriorityTaskWrapper wrapper = taskWrapperMap.get(taskId);
        Future<?> future = taskFutureMap.get(taskId);

        if (wrapper == null || future == null) {
            return false;
        }

        boolean cancelledByWrapper = wrapper.cancel();

        boolean cancelledByFuture = future.cancel(true);


        if (cancelledByWrapper || cancelledByFuture) {
            taskWrapperMap.remove(taskId);
            taskFutureMap.remove(taskId);
            log.info("任务:{}取消成功", taskId);
            return true;
        }

        return false;
    }


    /**
     * 清除已经完成的任务
     * 遍历所有任务，移除已完成的任务（包括正常完成、取消或异常结束）
     */
    public void cleanupCompletedTasks() {
        int initialSize = taskFutureMap.size();

        Iterator<Map.Entry<String, Future<?>>> iterator = taskFutureMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Future<?>> entry = iterator.next();
            String taskId = entry.getKey();
            Future<?> future = entry.getValue();

            if (future.isDone()) {
                iterator.remove();
                taskWrapperMap.remove(taskId);
                log.debug("已清除已完成的任务: {}", taskId);
            }
        }

        int removedCount = initialSize - taskFutureMap.size();
        if (removedCount > 0) {
            log.info("已清除 {} 个已完成的任务", removedCount);
        }
    }


    /**
     * 检查队列是否已满
     */
    public boolean isQueueFull() {
        return taskExecutor.getQueue().remainingCapacity() == 0;
    }


}

