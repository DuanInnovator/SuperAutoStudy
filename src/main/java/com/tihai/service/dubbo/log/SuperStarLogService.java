package com.tihai.service.dubbo.log;

import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.dubbo.query.log.WkLogQuery;

import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description : Dubbo日志服务接口
 * @Author : DuanInnovator
 * @CreateTime : 2025/5/19
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public interface SuperStarLogService {

    /**
     * 根据订单号获取日志
     * @param query 查询条件
     * @return 日志列表
     */
    List<SuperStarLog> getLogListByOrderId(WkLogQuery query);

    /**
     * 根据订单号获取最新一条日志
     * @param orderId 订单id
     * @return 最新一条日志
     */
    SuperStarLog getLatestLogByOrderId(String orderId);
}

