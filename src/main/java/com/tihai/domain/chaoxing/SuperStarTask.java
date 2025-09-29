package com.tihai.domain.chaoxing;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@TableName("th_order_wk")
@NoArgsConstructor
public class SuperStarTask {

    /**
     * 订单ID
     */
    private String orderId;

    @TableId(value = "sub_order_id")
    private String subOrderId;

    /**
     * 登录账
     */
    private String loginAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 课程id
     */
    private String courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 总价格
     */
    private BigDecimal totalPrice;

    /**
     * 优先级
     */
    private Integer priority;

//    /**
//     * 平台ID
//     */
//    private Long platform;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 订单参考进度
     */
    private BigDecimal process;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 机器编号
     */
    private String machineNum;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    public SuperStarTask(String orderId, Integer status) {
        this.orderId = orderId;
        this.status = status;
    }


}

