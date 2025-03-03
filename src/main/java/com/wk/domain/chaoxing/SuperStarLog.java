package com.wk.domain.chaoxing;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

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
public class SuperStarLog {
    /**
     * id
     */
    private Long id;

    /**
     * 登录账号
     */
    private String loginAccount;

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
     * 状态
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 备注
     */
    private String remark;
}

