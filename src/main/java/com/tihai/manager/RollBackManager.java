package com.tihai.manager;

import org.springframework.stereotype.Component;

/**
 * @Copyright : DuanInnovator
 * @Description :回滚管理器
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/27
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
public class RollBackManager {
    private int rollbackTimes = 0;
    private String rollbackId = "";

    public void addTimes(String id) throws Exception {
        if (id.equals(rollbackId) && rollbackTimes >= 3) {
            throw new RuntimeException("回滚次数已达3次，请手动检查学习通任务点完成情况");
        } else if (!id.equals(rollbackId)) {
            // 新任务
            rollbackId = id;
            rollbackTimes = 1;
        } else {
            rollbackTimes++;
        }
    }
}

