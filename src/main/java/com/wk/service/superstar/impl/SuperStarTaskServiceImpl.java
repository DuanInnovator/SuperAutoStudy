package com.wk.service.superstar.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wk.common.*;
import com.wk.constant.FlushProgressConstant;
import com.wk.constant.GlobalConstant;
import com.wk.constant.JobTypeConstant;
import com.wk.constant.RetryConstant;
import com.wk.domain.chaoxing.SuperStarLog;
import com.wk.domain.chaoxing.SuperStarTask;
import com.wk.domain.chaoxing.WkUser;
import com.wk.dto.CourseSubmitTaskDTO;
import com.wk.enums.WkTaskStatusEnum;
import com.wk.manager.RollBackManager;
import com.wk.mapper.SuperStarMapper;
import com.wk.queue.PriorityTaskWrapper;
import com.wk.service.superstar.SuperStarLogService;
import com.wk.service.superstar.SuperStarLoginService;
import com.wk.service.superstar.SuperStarTaskService;
import com.wk.service.superstar.SuperStarUserService;
import com.wk.utils.CourseUtil;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-服务实现
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@SuppressWarnings("all")
@Service
@Slf4j
public class SuperStarTaskServiceImpl extends ServiceImpl<SuperStarMapper, SuperStarTask> implements SuperStarTaskService {

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private ThreadPoolExecutor taskExecutor;

    @Autowired
    private SuperStarUserService userService;

    @Autowired
    private CourseUtil courseUtil;

    @Autowired
    private SuperStarLogService superStarLogService;
    @Autowired
    private SuperStarLoginService loginService;

    @Autowired
    private RollBackManager rb;


    /**
     * 添加超星学习任务
     *
     * @param courseSubmitTaskDTO
     */
    public void addChaoXingTask(CourseSubmitTaskDTO courseSubmitTaskDTO) {
        SuperStarTask chaoXingTask = mapperFacade.map(courseSubmitTaskDTO, SuperStarTask.class);
        chaoXingTask.setStatus(WkTaskStatusEnum.QUEUE.getCode());
        save(chaoXingTask);
    }

    /**
     * 将超星学习等待任务加入到任务队列中，并执行任务
     */
    @Override
    public void startChaoxingTask() {
        LambdaQueryWrapper<SuperStarTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuperStarTask::getStatus, WkTaskStatusEnum.PENDING.getCode());
        wrapper.le(SuperStarTask::getRetryCount, RetryConstant.DEFAULT_RETRY_COUNT);
        wrapper.orderByDesc(SuperStarTask::getPriority);
        List<SuperStarTask> tasks = list(wrapper);

        tasks.forEach(task -> {
            task.setStatus(WkTaskStatusEnum.QUEUE.getCode()); // 先设置状态
            boolean isUpdated = this.updateById(task); // 更新数据库状态
            Runnable taskWrapper = new PriorityTaskWrapper(() -> {
                try {

                    executeCourseTask(task);
                } catch (Exception e) {
                    log.error("任务执行异常, taskId={}，异常信息：", task.getId(), e);
                    if(task.getRetryCount()==RetryConstant.DEFAULT_RETRY_COUNT){
                        task.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                    }else {
                        task.setRetryCount(task.getRetryCount() + 1);
                        task.setStatus(WkTaskStatusEnum.PENDING.getCode());
                    }

                    this.updateById(task); // 更新任务为 ABNORMAL 状态
                }
            }, task.getPriority(), task.getId());

            taskExecutor.execute(taskWrapper); // 启动任务执行
        });

    }

    /**
     * 执行超星学习任务
     * @param task
     */
    public void executeCourseTask(SuperStarTask task) {
        WkUser user = userService.getUserByAccount(task.getLoginAccount());
        SuperStarLog log = superStarLogService.getLatestLogByLoginAccount(task.getLoginAccount(), task.getCourseName());

        if (log == null) {
            log = mapperFacade.map(task, SuperStarLog.class);
            log.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
            superStarLogService.saveLog(log);
        } else {
            log.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
            superStarLogService.updateById(log);
        }
        if (user == null || user.getCookies() == null) {
            if(task.getRetryCount()<RetryConstant.DEFAULT_RETRY_COUNT){
                task.setStatus(WkTaskStatusEnum.PENDING.getCode());
                task.setRetryCount(task.getRetryCount() + 1);
            }else {
                task.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
            }
            WkUser wkUser = new WkUser();
            wkUser.setAccount(task.getLoginAccount());
            wkUser.setPassword(task.getPassword());
            loginService.login(wkUser);
            this.updateById(task);
            log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
            log.setErrorMessage("用户未登录");
            superStarLogService.saveLog(log);
            return;
        }

        try {
            courseUtil.setCookies(user.getCookies());
            Course readyCourse = courseUtil.getCourseList().stream().filter(course -> course.getCourseId().equals(task.getCourseId())).findFirst().orElse(null);
            if (readyCourse == null) {
                log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                log.setErrorMessage(GlobalConstant.COURSE_INFO_GET_FAIL);
                superStarLogService.saveLog(log);
                return;
            }

            List<CoursePoint> pointList = courseUtil.getCoursePoint(
                    readyCourse.getCourseId(), readyCourse.getClazzId(), readyCourse.getCpi());

            processCoursePoints(task, log, readyCourse, pointList);
        } catch (Exception e) {
            log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
            log.setErrorMessage("系统异常");
            superStarLogService.saveLog(log);
        }
    }

    private void processCoursePoints(SuperStarTask task, SuperStarLog log, Course readyCourse, List<CoursePoint> pointList) {
        if (log == null) {
            log = mapperFacade.map(task, SuperStarLog.class);
            log.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
            superStarLogService.saveLog(log);
        }

        int pointIndex = log.getCurrentChapterIndex() != null ? log.getCurrentChapterIndex() : 0;
        List<ChapterPoint> chapterPointList = pointList.get(0).getPoints();

        while (pointIndex < chapterPointList.size()) {
            try {
                log.setCurrentChapterIndex(pointIndex);
                ChapterPoint chapterPoint = chapterPointList.get(pointIndex);
                Pair<List<Job>, List<JobInfo>> result = courseUtil.getJobList(
                        readyCourse.getClazzId().toString(),
                        readyCourse.getCourseId(),
                        readyCourse.getCpi(),
                        chapterPoint.getId());

                List<Job> jobs = result.getFirst();
                List<JobInfo> jobInfo = result.getSecond();

                if (jobs.isEmpty()) {
                    pointIndex++;
                    continue;
                }

                // 任务是否被锁定
                if (jobs.stream().findFirst().map(job -> job instanceof Map && Boolean.TRUE.equals(((Map<?, ?>) job).get("notOpen"))).orElse(false)) {
                    pointIndex--;
                    rb.addTimes(chapterPoint.getId());
                    continue;
                }

                for (Job job : jobs) {
                    try {
                        switch (job.getType()) {
                            case JobTypeConstant.VIDEO:
                                boolean isAudio = false;
                                try {
                                    courseUtil.studyVideo(readyCourse, job, jobInfo.get(0), 2.00, "Video", log);
                                } catch (Exception e) {
                                    isAudio = true;
                                }
                                if (isAudio) {
                                    try {
                                        courseUtil.studyVideo(readyCourse, job, jobInfo.get(0), 1.00, "Audio", log);
                                    } catch (Exception e) {
                                        log.setErrorMessage("异常任务 -> 章节: " + job.getId() + "，已跳过");
                                    }
                                }
                                break;
                            case JobTypeConstant.DOCUMENT:
                                courseUtil.studyDocument(readyCourse, job);
                                break;
                            case JobTypeConstant.READ:
                                courseUtil.studyRead(readyCourse, job, jobInfo.get(0), log);
                                break;
                            case JobTypeConstant.QUESTION:
                                courseUtil.studyWork(readyCourse, job, jobInfo.get(0));
                            default:
                                break;
                        }
                        superStarLogService.saveLog(log);
                    } catch (Exception e) {
                        log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                        log.setErrorMessage("任务异常，任务ID=" + job.getTitle());
                        superStarLogService.saveLog(log);
                    }
                }

                pointIndex++;

                // 计算进度阈值
                int totalChapters = chapterPointList.size();
                double currentPercentage = (pointIndex * 100.0) / totalChapters;
                int currentThreshold = (int) (Math.floor(currentPercentage / 5) * 5);

                if (currentThreshold > FlushProgressConstant.MAX_PROGRESS) {
                    log.setCurrentChapterIndex(pointIndex - 1);
                    log.setCurrentProgress(BigDecimal.valueOf(currentPercentage));
                    superStarLogService.saveLog(log);
                }

            } catch (Exception e) {
                log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                log.setErrorMessage("章节处理异常");
                superStarLogService.saveLog(log);
                break;
            }
        }

        if (pointIndex >= pointList.size()) {
            log.setCurrentChapterIndex(pointIndex - 1);
            log.setCurrentProgress(BigDecimal.valueOf(100));
            log.setStatus(WkTaskStatusEnum.FINISHED.getCode());
            superStarLogService.saveLog(log);
        }
    }

}

