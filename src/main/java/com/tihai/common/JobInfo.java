package com.tihai.common;

import lombok.Data;

/**
 * @Copyright : DuanInnovator
 * @Description : 任务信息
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
@SuppressWarnings("all")
public class JobInfo {
    private String ktoken;
    private String mtEnc;
    private int reportTimeInterval;
    private String defenc;
    private String cardid;
    private String cpi;
    private String qnenc;
    private String knowledgeid;
    private Boolean notOpen;
}

