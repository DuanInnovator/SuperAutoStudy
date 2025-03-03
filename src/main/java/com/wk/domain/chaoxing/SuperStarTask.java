package com.wk.domain.chaoxing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通任务实体
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("th_wk_queue")
public class SuperStarTask {

    /**
     * 订单id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private String id;

    /**
     * 账号
     */
    private String loginAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程id
     */
    private String courseId;

    /**
     * 优先级 设定1-10,1为最高优先级
     */
    private Integer priority;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 重试次数
     */
    private Integer retryCount;



}

