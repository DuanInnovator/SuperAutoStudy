package com.tihai.dubbo.query.log;

import com.tihai.query.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Copyright : DuanInnovator
 * @Description : 网课日志查询
 * @Author : DuanInnovator
 * @CreateTime : 2025/5/20
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class WkLogQuery extends Query implements Serializable {

    /**
     * 订单id
     */
    private String orderId;
}

