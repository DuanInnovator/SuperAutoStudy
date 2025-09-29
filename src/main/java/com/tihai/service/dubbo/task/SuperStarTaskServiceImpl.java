package com.tihai.service.dubbo.task;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.domain.chaoxing.SuperStarTask;
import com.tihai.enums.BizCodeEnum;
import com.tihai.enums.WkTaskStatusEnum;
import com.tihai.exception.BusinessException;
import com.tihai.mapper.SuperStarLogMapper;
import com.tihai.mapper.SuperStarTaskMapper;
import com.tihai.service.dubbo.log.SuperStarLogService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Copyright : DuanInnovator
 * @Description : Dubbo-超星学习通任务接口实现
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@DubboService
@Service(value = "Dubbo-SuperStarTaskService")
public class SuperStarTaskServiceImpl implements SuperStarTaskService {


    @Autowired
    private SuperStarTaskMapper mapper;

    @Autowired
    private SuperStarLogMapper logMapper;

    @Autowired
    private SuperStarLogService service;

    /**
     * 暂停任务
     *
     * @param subOrderId 子订单id
     */
    @Override
    public void pauseTaskByOrderId(String subOrderId) {
        LambdaQueryWrapper<SuperStarTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuperStarTask::getSubOrderId, subOrderId);
        SuperStarTask superStarTask = mapper.selectOne(wrapper);
        if (superStarTask != null) {
            if (superStarTask.getStatus().equals(WkTaskStatusEnum.FINISHED.getCode())) {
                throw new BusinessException(BizCodeEnum.TASK_ALREADY_FINISHED.getCode(), BizCodeEnum.TASK_ALREADY_FINISHED.getMsg());
            } else {
                if (superStarTask.getStatus().equals(WkTaskStatusEnum.ABNORMAL.getCode())) {
                    throw new BusinessException(BizCodeEnum.TASK_ERROR.getCode(), BizCodeEnum.TASK_ERROR.getMsg());
                }
                if (superStarTask.getStatus().equals(WkTaskStatusEnum.EXAM.getCode())) {
                    throw new BusinessException(BizCodeEnum.TASK_EXAM.getCode(), BizCodeEnum.TASK_ERROR.getMsg());
                }
                if (!superStarTask.getStatus().equals(WkTaskStatusEnum.TOUCH_UP.getCode())) {
                    superStarTask.setStatus(WkTaskStatusEnum.PAUSED.getCode());
                    superStarTask.setPriority(0);
                }

                SuperStarLog latestLogByOrderId = service.getLatestLogByOrderId(subOrderId);
                if (latestLogByOrderId != null && latestLogByOrderId.getStatus().equals(WkTaskStatusEnum.PROCESSING.getCode())) {
                    latestLogByOrderId.setStatus(WkTaskStatusEnum.PAUSED.getCode());
                    latestLogByOrderId.setRemark(BizCodeEnum.TASK_PAUSED.getMsg());
                    logMapper.updateById(latestLogByOrderId);
                }
                mapper.updateById(superStarTask);
            }
        } else {
            throw new BusinessException(BizCodeEnum.TASK_NOT_EXIST.getCode(), BizCodeEnum.TASK_NOT_EXIST.getMsg());
        }

    }

    /**
     * 补刷任务
     *
     * @param subOrderId 子订单id
     */
    @Override
    public void touchUpTaskByOrderId(String subOrderId) {
        LambdaQueryWrapper<SuperStarTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuperStarTask::getOrderId, subOrderId);
        SuperStarTask superStarTask = mapper.selectOne(wrapper);
        if (superStarTask != null) {
            if (superStarTask.getStatus().equals(WkTaskStatusEnum.TOUCH_UP.getCode())) {
                throw new BusinessException(BizCodeEnum.TASK_AlREADY_TOUCH_UP.getCode(), BizCodeEnum.TASK_AlREADY_TOUCH_UP.getMsg());
            }
            superStarTask.setPriority(-1);
            superStarTask.setStatus(WkTaskStatusEnum.TOUCH_UP.getCode());
            mapper.updateById(superStarTask);
        } else {
            throw new BusinessException(BizCodeEnum.TASK_NOT_EXIST.getCode(), BizCodeEnum.TASK_NOT_EXIST.getMsg());
        }
    }
}