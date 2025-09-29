package com.tihai.enums;

/**
 * @Copyright : DuanInnovator
 * @Description : 网课任务状态枚举
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link :<a href="https://github.com/DuanInnovator/SuperAutoStudy">...</a>
 **/
public enum WkTaskStatusEnum {
    PENDING(0, "待处理"),
    PROCESSING(1, "进行中"),
    QUEUE(2, "队列中"),
    EXAM(3, "考试中"),
    FINISHED(4, "已完成"),
    ABNORMAL(5, "异常"),
    PAUSED(6, "暂停中"),

    TOUCH_UP(7, "补刷中");

    private final Integer code;
    private final String status;

    WkTaskStatusEnum(Integer code, String status) {
        this.code = code;
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

}

