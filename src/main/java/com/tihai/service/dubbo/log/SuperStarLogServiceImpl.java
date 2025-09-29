package com.tihai.service.dubbo.log;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tihai.common.PageResult;

import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.dubbo.query.log.WkLogQuery;
import com.tihai.mapper.SuperStarLogMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description : Dubbo-日志记录接口实现
 * @Author : DuanInnovator
 * @CreateTime : 2025/5/20
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@DubboService
@Service(value = "Dubbo-SuperStarLogService")
public class SuperStarLogServiceImpl implements SuperStarLogService {

    @Autowired
    private SuperStarLogMapper superStarLogMapper;

    /**
     * 根据订单id获取日志列表
     *
     * @param query 查询条件
     * @return 日志列表
     */
    @Override
    public List<SuperStarLog> getLogListByOrderId(WkLogQuery query) {
        LambdaQueryWrapper<SuperStarLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuperStarLog::getSubOrderId, query.getOrderId());
        wrapper.orderByDesc(SuperStarLog::getEndTime);
        Page<SuperStarLog> page = new Page<>(query.getPage(), query.getPageSize());

        Page<SuperStarLog> orderPage = superStarLogMapper.selectPage(page, wrapper);

        PageResult<SuperStarLog> result = new PageResult<>();
        result.setTotal(orderPage.getTotal());
        result.setPage(query.getPage());
        result.setPageSize(query.getPageSize());
        result.setData(orderPage.getRecords());
        return result.getData();
    }

    /**
     * 根据订单id获取最新一条日志
     *
     * @param subOrderId 订单id
     * @return 最新一条日志
     */
    @Override
    public SuperStarLog getLatestLogByOrderId(String subOrderId) {
        LambdaQueryWrapper<SuperStarLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuperStarLog::getSubOrderId, subOrderId);
        wrapper.orderByDesc(SuperStarLog::getEndTime);
        wrapper.last("limit 1");

        return superStarLogMapper.selectOne(wrapper);
    }
}

