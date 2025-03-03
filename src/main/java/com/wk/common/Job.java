package com.wk.common;

import lombok.Data;

/**
 * @Copyright : DuanInnovator
 * @Description : 任务点集合
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
public class Job {

    /**
     * 任务点标题
     */
    private String title;

    private String name;


    /**
     * 任务点类型
     */
    private String type;

    /**
     * 任务点id
     */
    private String id;

    /**
     * id
     */
    private String jobId;

    /**
     * objectId
     */
    private String objectId;

    /**
     * token
     */
    private String jToken;
    /**
     * mid
     */
    private String mid;

    /**
     * info
     */
    private String otherInfo;

    /**
     * enc
     */
    private String enc;

    /**
     * aid
     */
    private String aid;
}

