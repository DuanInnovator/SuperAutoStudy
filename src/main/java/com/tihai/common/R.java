package com.tihai.common;


import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.tihai.enums.BizCodeEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * @Copyright : DuanInnovator
 * @Description : 统一响应结果
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class R extends HashMap<String, Object> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 当前请求id
     */
    private String requestId = IdUtil.objectId().toUpperCase(Locale.CHINA);

    /**
     * 系统时间
     */

//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String systemTime = LocalDateTime.now().toString().replace("T", " ");

    /**
     * 响应码
     * 200：成功  ：201失败
     */

    private String code;
    /**
     * 响应消息
     * msg错误信息返回
     */

    private String msg;
    /**
     * 响应子码
     * 具体接口返回状态:BizCodeEnum详情
     */

    private String subCode;


    public R setData(Object data) {
        put("data", data);
        return this;
    }



    public R() {
        put("code", 200);
        put("msg", "success");
        put("data", null);
        put("requestId", requestId);
        put("systemTime", systemTime);
    }



    public static R error(int subCode, String msg) {
        R r = new R();
        r.put("code", 201);
        r.put("subCode", subCode);
        r.put("msg", msg);
        return r;
    }

    public static R error(int code, String msg, Object data) {
        R r = new R();
        r.put("code", 201);
        r.put("subCode", code);
        r.put("msg", msg);
        r.put("data", data);
        return r;
    }


    public static R ok(String msg) {
        R r = new R();
        r.put("code", 200);
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.put("code", 200);
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public static R ok(int subCode, String msg, Object value) {
        R r = new R();
        r.put("code", 200);
        r.put("subCode", subCode);
        r.put("msg", msg);
        r.put("data", value);
        return r;
    }

    public static R ok(int subCode, String msg) {
        R r = new R();
        r.put("code", 200);
        r.put("subCode", subCode);
        r.put("msg", msg);
        return r;
    }


    public R put(String key, Object value) {

        super.put(key, value);
        return this;
    }

    public R put(BizCodeEnum bizCodeEnum) {
        put("code", 200);
        put("subCode", bizCodeEnum.getCode());
        put("msg", bizCodeEnum.getMsg());
        return this;
    }

    public R put(Object value) {
        super.put("data", value);
        return this;
    }

    public R put(int subCode, String msg) {

        put("code", 200);
        put("subCode", subCode);
        put("msg", msg);
        return this;
    }


    public Integer getCode() {

        return (Integer) this.get("code");
    }
}
