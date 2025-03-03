package com.wk.dto;

import com.wk.constant.GlobalConstant;
import lombok.Data;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

/**
 * @Copyright : DuanInnovator
 * @Description : 课程订单任务DTO
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/28
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
public class CourseSubmitTaskDTO {

    /**
     * 登录账号
     */
    @NotBlank(message = GlobalConstant.ACCOUNT_NOT_NULL)
    private String loginAccount;

    /**
     * 密码
     */
    @NotBlank(message = GlobalConstant.PASSWORD_NOT_NULL)
    private String password;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 代刷课程名称
     */

    private String courseName;

    @NotBlank(message = GlobalConstant.COURSE_ID_NOT_NULL)
    private String courseId;
}

