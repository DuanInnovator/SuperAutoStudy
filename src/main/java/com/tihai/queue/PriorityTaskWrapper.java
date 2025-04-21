package com.tihai.queue;


/**
 * @Copyright : DuanInnovator
 * @Description :
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/

public class PriorityTaskWrapper implements Runnable, Comparable<PriorityTaskWrapper> {
    private final Runnable realTask;
    private int priority;
    private final String taskId;

    public PriorityTaskWrapper(Runnable realTask, int priority, String taskId) {
        this.realTask = realTask;
        this.priority = priority;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        realTask.run();
    }

    @Override
    public int compareTo(PriorityTaskWrapper other) {
        return Integer.compare(this.priority,other.priority); // 降序排列
    }

    public void decreasePriority(int delta) {
        this.priority += delta;
    }

    // Getters
    public int getPriority() { return priority; }
    public String getTaskId() { return taskId; }
}