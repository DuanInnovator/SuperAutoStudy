package com.wk.common;

import lombok.Data;

/**
 * @Copyright : DuanInnovator
 * @Description : 课程类
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
public class Course {

    /**
     * id
     */
    private String id;

    /**
     * 课程信息
     */
    private String info;

    /**
     * 角色id
     */
    private String roleId;

    /**
     * 班级id
     */
    private String clazzId;

    /**
     * 课程id
     */
    private String courseId;

    /**
     * 课程点id
     */
    private String cpi;

    /**
     * 课程名称
     */
    private String title;

    /**
     * 课程描述
     */
    private String desc;

    /**
     * 课程老师
     */
    private String teacher;
}

