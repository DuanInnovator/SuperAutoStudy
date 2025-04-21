package com.tihai.factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Copyright : DuanInnovator
 * @Description : 消费线程工厂
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/2
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class CustomThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final AtomicInteger threadCount = new AtomicInteger(1);

    public CustomThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, namePrefix + threadCount.getAndIncrement());
    }
}