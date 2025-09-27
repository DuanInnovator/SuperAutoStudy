package com.tihai.exception;

import java.io.Serializable;

/**
 * @Copyright : DuanInnovator
 * @Description : 业务异常
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/26
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
public class BusinessException extends RuntimeException implements Serializable {
    private Integer code;
    private Object data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public BusinessException(Integer code) {
        this.code = code;
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(Integer code, String msg, Object data) {
        super(msg);
        this.code = code;
        this.data=data;
    }

    public BusinessException(Integer code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    public String getMsg() {
        return super.getMessage();
    }
}