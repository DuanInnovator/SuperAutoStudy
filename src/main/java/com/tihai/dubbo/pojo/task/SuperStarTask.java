package com.tihai.dubbo.pojo.task;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @TableId(value = "id")
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

    /**
     * 创建时间
     */
    private LocalDateTime creatTime;

    /**
     * 分数
     */
    private BigDecimal score;

    /**
     * 机器码
     */
    private String machineNum;

}

