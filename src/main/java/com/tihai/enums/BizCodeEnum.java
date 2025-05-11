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


   TASK_ALREADY_EXIST(20001, "任务已存在"),

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
