package com.tihai.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @Copyright : DuanInnovator
 * @Description : 全局状态码枚举
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@AllArgsConstructor
@NoArgsConstructor

public enum BizCodeEnum {

    ACCOUNT_OR_PASSWORD_ERROR(10001, "账号或密码错误"),

    TASK_NOT_EXIST(20000, "任务不存在"),
    TASK_ALREADY_EXIST(20001, "任务已存在"),

    TASK_ALREADY_FINISHED(20002, "任务已完成"),

    TASK_PAUSED(20004, "任务已暂停"),

    TASK_AlREADY_TOUCH_UP(20005, "任务在补刷"),

    TASK_ERROR(20006, "任务异常"),

    TASK_EXAM(20007, "任务在考试中"),

    LOG_NOT_EXIST(30003, "日志不存在"),

    SYSTEM_ERROR(102, "系统异常"),

    METHOD_ARGUMENT_NOT_VALID_CODE(111, "参数非法");

    private Integer code;
    private String msg;

    public Integer getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }


}
