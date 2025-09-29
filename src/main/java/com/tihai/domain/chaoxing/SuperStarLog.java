package com.tihai.domain.chaoxing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-日志信息
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("th_wk_log")
public class SuperStarLog implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 子订单号
     */
    private String subOrderId;

    /**
     * 登录账号
     */
    private String loginAccount;

    /**
     * 课程id
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 当前任务点
     */
    private String currentJob;

    /**
     * 当前任务点id
     */
    private Integer currentJobId;

    /**
     * 当前章节索引
     */
    private Integer currentChapterIndex;

    /**
     * 当前进度
     */
    private BigDecimal currentProgress;

    /**
     * 状态  0-待处理，1-进行中，2-队列中，3-考试中，4-已完成，5-异常
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 机器码
     */
    private String machineNum;

    /**
     * 是否删除
     */
    private Integer isDelete;
}

