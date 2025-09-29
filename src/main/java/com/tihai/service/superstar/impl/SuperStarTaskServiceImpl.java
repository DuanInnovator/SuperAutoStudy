package com.tihai.service.superstar.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tihai.common.*;
import com.tihai.constant.GlobalConstant;
import com.tihai.constant.JobTypeConstant;
import com.tihai.constant.RetryConstant;
import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.domain.chaoxing.SuperStarTask;
import com.tihai.domain.chaoxing.WkUser;
import com.tihai.dubbo.dto.CourseSubmitTaskDTO;
import com.tihai.dubbo.pojo.course.Course;
import com.tihai.enums.BizCodeEnum;
import com.tihai.enums.WkTaskStatusEnum;
import com.tihai.exception.BusinessException;
import com.tihai.manager.GlobalCookieManager;
import com.tihai.manager.RollBackManager;
import com.tihai.manager.TaskManager;
import com.tihai.mapper.SuperStarMapper;
import com.tihai.properties.StudyProperties;
import com.tihai.properties.ThreadPoolProperties;
import com.tihai.queue.PriorityTaskWrapper;
import com.tihai.service.superstar.SuperStarLogService;
import com.tihai.service.superstar.SuperStarLoginService;
import com.tihai.service.superstar.SuperStarTaskService;
import com.tihai.utils.*;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import okhttp3.CookieJar;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.tihai.constant.GlobalConstant.TASK_RETRY_MAX_COUNT;


/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-任务启动和实现
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Service
@Slf4j
public class SuperStarTaskServiceImpl extends ServiceImpl<SuperStarMapper, SuperStarTask> implements SuperStarTaskService {

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private CourseUtil courseUtil;

    @Autowired
    private VideoUtil videoUtil;

    @Autowired
    private DocumentAndReadUtil documentAndReadUtil;

    @Autowired
    private WorkUtil workUtil;

    @Autowired
    private SuperStarLogService superStarLogService;

    @Autowired
    private SuperStarLoginService loginService;


    @Autowired
    private RollBackManager rb;

    @Autowired
    private ServerInfoUtil serverInfoUtil;

    @Autowired
    private ThreadPoolProperties config;

    @Autowired
    private StudyProperties studyProperties;


    private volatile boolean isShuttingDown = false;
    private final Object shutdownLock = new Object();


    public SuperStarTaskServiceImpl() throws NacosException {
    }

//    public void shutdownThreadPoolGracefully() throws NacosException {
//        // 提前检查减少同步块竞争
//        if (isShuttingDown || taskExecutor.isShutdown()) {
//            return;
//        }
//
//        synchronized (shutdownLock) {
//            // 再次检查（双重检查锁模式）
//            if (isShuttingDown || taskExecutor.isShutdown()) {
//                return;
//            }
//            isShuttingDown = true;
//
//            try {
//                // 1. 停止接受新任务
//                taskExecutor.shutdown();
//                log.info("线程池开始关闭，等待现有任务完成...");
//
//                // 2. 分阶段等待
//                // 第一阶段：等待正常完成
//                if (!taskExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
//                    log.warn("等待超时，尝试强制停止剩余任务...");
//
//                    // 第二阶段：强制停止
//                    List<Runnable> unfinishedTasks = taskExecutor.shutdownNow();
//
//                    // 3. 处理未完成任务
//                    if (!unfinishedTasks.isEmpty()) {
//                        log.warn("有{}个任务被强制停止，开始回滚状态...", unfinishedTasks.size());
//                        processUnfinishedTasks(unfinishedTasks, true);
//                    }
//
//                    // 最后检查
//                    if (!taskExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
//                        log.error("线程池最终未能完全关闭");
//                    }
//                }
//            } catch (InterruptedException e) {
//                // 处理中断
//                List<Runnable> unfinishedTasks = taskExecutor.shutdownNow();
//                if (!unfinishedTasks.isEmpty()) {
//                    processUnfinishedTasks(unfinishedTasks, true);
//                }
//                Thread.currentThread().interrupt();
//                throw new NacosException(NacosException.SERVER_ERROR, "线程池关闭被中断");
//            } finally {
//                log.info("线程池关闭完成，当前状态: isShutdown={}, isTerminated={}",
//                        taskExecutor.isShutdown(), taskExecutor.isTerminated());
//                processUnfinishedTasks(null, false);
//
//            }
//        }
//    }

    /**
     * 暂停所有未完成任务
     *
     * @param unfinishedTasks 未完成任务列表
     * @param flag            flag
     */
    private void processUnfinishedTasks(List<Runnable> unfinishedTasks, boolean flag) {
        try {
            if (flag) {
                unfinishedTasks.forEach(task -> {
                    if (task instanceof PriorityTaskWrapper) {
                        String taskId = ((PriorityTaskWrapper) task).getTaskId();

                        SuperStarTask dbTask = this.getById(taskId);
                        if (dbTask != null &&
                                (WkTaskStatusEnum.PROCESSING.getCode().equals(dbTask.getStatus()) ||
                                        WkTaskStatusEnum.EXAM.getCode().equals(dbTask.getStatus()))) {
                            dbTask.setStatus(WkTaskStatusEnum.PAUSED.getCode());
                            this.updateById(dbTask);
                        }

                    }
                });
            } else {
                List<SuperStarTask> processingTasks = this.getProcessingTasks();
                processingTasks.forEach(task -> {
                    task.setStatus(WkTaskStatusEnum.PAUSED.getCode());
                    this.updateById(task);
                });
            }
        } catch (Exception e) {
            log.error("回滚状态失败:{}", e.getMessage());
        }
    }

    /**
     * 重启线程池和任务
     */
//    public void restartThreadPoolAndTasks() {
//        synchronized (shutdownLock) {
//            if (!taskExecutor.isShutdown()) {
//                return;
//            }
//
//            BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(config.getQueueCapacity());
//
//            // 重新构建线程池
//            taskExecutor = new ThreadPoolExecutor(
//                    config.getCoreSize(),
//                    config.getMaxSize(),
//                    config.getKeepAlive(),
//                    TimeUnit.SECONDS,
//                    queue,
//                    new CustomThreadFactory(config.getThreadNamePrefix()),
//                    new PriorityRejectPolicy()
//            );
//            taskExecutor.allowCoreThreadTimeOut(config.isAllowCoreThreadTimeout());
//            this.startChaoxingTask();
//        }
//    }


    /**
     * 根据账号和课程id获取超星学习任务
     *
     * @param loginAccount 账号
     * @param courseId     课程id
     */
    @Override
    public SuperStarTask getSuperStarTask(String loginAccount, String courseId) {
        LambdaQueryWrapper<SuperStarTask> superStarTaskLambdaQueryWrapper = new LambdaQueryWrapper<>();
        superStarTaskLambdaQueryWrapper.eq(SuperStarTask::getLoginAccount, loginAccount);
        superStarTaskLambdaQueryWrapper.eq(SuperStarTask::getCourseId, courseId);
        return this.getOne(superStarTaskLambdaQueryWrapper);
    }

    /**
     * 获取所有正在处理的任务
     *
     * @return 正在处理的任务
     */
    @Override
    public List<SuperStarTask> getProcessingTasks() {
        return this.list(new LambdaQueryWrapper<SuperStarTask>().eq(SuperStarTask::getStatus, WkTaskStatusEnum.PROCESSING.getCode()));
    }

    /**
     * 根据账号和课程id获取超星学习任务
     *
     * @param loginAccount 账号
     * @param courseName   课程名
     */
    @Override
    public SuperStarTask getSuperStarTaskByCourseName(String loginAccount, String courseName) {
        LambdaQueryWrapper<SuperStarTask> superStarTaskLambdaQueryWrapper = new LambdaQueryWrapper<>();
        superStarTaskLambdaQueryWrapper.eq(SuperStarTask::getLoginAccount, loginAccount);
        superStarTaskLambdaQueryWrapper.eq(SuperStarTask::getCourseName, courseName);
        return this.getOne(superStarTaskLambdaQueryWrapper);
    }

    /**
     * 更新任务
     *
     * @param task 任务
     */
    @Override
    public void updateSuperStarTask(SuperStarTask task) {
        this.baseMapper.updateById(task);
    }

    /**
     * 批量更新任务
     *
     * @param tasks 任务
     */
    @Override
    public void batchUpdateSuperStarTask(List<SuperStarTask> tasks) {
        this.saveOrUpdateBatch(tasks);
    }

    /**
     * 添加任务到等待队列
     *
     * @param courseSubmitTaskDTO 任务信息
     * @throws NacosException nacos异常
     */
    public void addChaoXingTask(CourseSubmitTaskDTO courseSubmitTaskDTO) throws NacosException {
        SuperStarTask task = null;
        if (courseSubmitTaskDTO.getCourseId() == null) {
            task = getSuperStarTaskByCourseName(courseSubmitTaskDTO.getLoginAccount(), courseSubmitTaskDTO.getCourseName());
        } else {
            task = getSuperStarTask(courseSubmitTaskDTO.getLoginAccount(), courseSubmitTaskDTO.getCourseId());
        }
        if (task == null) {
            SuperStarTask chaoXingTask = mapperFacade.map(courseSubmitTaskDTO, SuperStarTask.class);
            chaoXingTask.setStatus(WkTaskStatusEnum.PENDING.getCode());

            chaoXingTask.setPriority(1);
            chaoXingTask.setMachineNum(serverInfoUtil.getCurrentServerInstance());
            this.baseMapper.insert(chaoXingTask);
        } else {
            throw new BusinessException(BizCodeEnum.TASK_ALREADY_EXIST.getCode(), BizCodeEnum.TASK_ALREADY_EXIST.getMsg());
        }
    }

    /**
     * 超星学习任务启动
     */
    @Override
    public void startChaoxingTask() {
        // 1. 查询待处理任务 待处理,暂停,补刷
        LambdaQueryWrapper<SuperStarTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SuperStarTask::getStatus,
                WkTaskStatusEnum.PENDING.getCode(),
                WkTaskStatusEnum.TOUCH_UP.getCode());
        wrapper.le(SuperStarTask::getRetryCount, RetryConstant.DEFAULT_RETRY_COUNT);
        wrapper.orderByDesc(SuperStarTask::getPriority);
        wrapper.orderByDesc(SuperStarTask::getCreateTime);
        List<SuperStarTask> tasks = list(wrapper);
        tasks = tasks.stream().filter(task -> {
            if (task.getStatus().equals(WkTaskStatusEnum.TOUCH_UP.getCode())) {
                SuperStarLog log = superStarLogService.getLatestLogByLoginAccount(task.getLoginAccount(), task.getCourseName(), task.getCourseId());
                return log == null;
            }
            return true;
        }).collect(Collectors.toList());

        tasks.forEach(task -> {
            try {
                if (taskManager.isQueueFull()) {
                    log.warn("任务队列已满，将任务{}重置为PENDING状态", task.getOrderId());
                    if (!task.getStatus().equals(WkTaskStatusEnum.TOUCH_UP.getCode())) {
                        task.setStatus(WkTaskStatusEnum.QUEUE.getCode());
                    }

                    updateById(task);
                    return;
                }

                if (!task.getStatus().equals(WkTaskStatusEnum.TOUCH_UP.getCode())) {
                    task.setStatus(WkTaskStatusEnum.QUEUE.getCode());
                }
                task.setMachineNum(serverInfoUtil.getCurrentServerInstance());
                updateById(task);
                PriorityTaskWrapper taskWrapper = new PriorityTaskWrapper(() -> executeWithRetry(task),
                        task.getPriority(),
                        task.getSubOrderId());

                // 6. 带拒绝策略的任务提交
                try {
                    taskManager.submitTask(taskWrapper);
                } catch (RejectedExecutionException e) {
                    handleRejectedTask(task);
                } catch (Exception e) {
                    log.error("任务提交失败: {}", e.getMessage(), e);
                }
            } catch (Exception e) {
                log.error("任务{}初始化异常", task.getOrderId(), e);
                resetTaskToPending(task);
            }
        });

    }

    /**
     * 重试的任务执行逻辑
     *
     * @param task 任务
     */
    private void executeWithRetry(SuperStarTask task) {
        try {

            GlobalCookieManager.setCurrentAccount(task.getLoginAccount());
            executeCourseTask(task);

        } catch (Exception e) {
            log.error("任务{}执行异常", task.getSubOrderId(), e);
            if (task.getRetryCount() >= RetryConstant.DEFAULT_RETRY_COUNT) {
                task.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
            } else {
                task.setRetryCount(task.getRetryCount() + 1);
                task.setStatus(WkTaskStatusEnum.PENDING.getCode());
            }
            updateById(task);
        }
    }

    /**
     * 拒绝任务处理逻辑
     *
     * @param task 拒绝的任务
     */
    private void handleRejectedTask(SuperStarTask task) {
        log.warn("线程池拒绝任务[ID:{}], 重置为PENDING状态", task.getOrderId());
        if (!task.getStatus().equals(WkTaskStatusEnum.TOUCH_UP.getCode())) {
            task.setStatus(WkTaskStatusEnum.PENDING.getCode());
        }
        task.setMachineNum(null);
        updateById(task);
    }

    /**
     * 重置任务状态
     *
     * @param task 待处理的任务
     */
    private void resetTaskToPending(SuperStarTask task) {
        try {
            task.setStatus(WkTaskStatusEnum.PENDING.getCode());
            updateById(task);
        } catch (Exception ex) {
            log.error("重置任务状态失败[ID:{}]", task.getOrderId(), ex);
        }
    }

    /**
     * 执行超星学习任务
     */
    public void executeCourseTask(SuperStarTask task) {
        SuperStarLog log = superStarLogService.getLatestLogByLoginAccount(
                task.getLoginAccount(), task.getCourseName(), task.getCourseId());

        if (log == null) {
            log = superStarLogService.createNewLog(task);
        } else {
            log.setId(IdWorker.getId());
            log.setErrorMessage(null);
            log.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
            log.setRemark(null);
        }

        try {
            // 重试逻辑
            if (task.getRetryCount() < RetryConstant.DEFAULT_RETRY_COUNT) {
                if (!task.getStatus().equals(WkTaskStatusEnum.TOUCH_UP.getCode())) {
                    task.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
                }
                task.setRetryCount(task.getRetryCount() + 1);
            } else {
                task.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                log.setErrorMessage(TASK_RETRY_MAX_COUNT);
                return;
            }

            // 用户登录
            WkUser wkUser = new WkUser();
            wkUser.setAccount(task.getLoginAccount());
            wkUser.setPassword(task.getPassword());
            CookieJar cookieJar = loginService.login(wkUser);
            if (cookieJar == null) {
                throw new RuntimeException("用户未登录");
            }
            this.updateById(task);

            // 获取课程信息
            Course readyCourse = getReadyCourse(task, log);
            if (readyCourse == null) {
                throw new RuntimeException(GlobalConstant.COURSE_INFO_GET_FAIL);
            }
            log.setCourseName(readyCourse.getTitle());

            List<CoursePoint> pointList = courseUtil.getCoursePoint(
                    task.getLoginAccount(),
                    readyCourse.getCourseId(),
                    readyCourse.getClazzId(),
                    readyCourse.getCpi()
            );

            // 处理课程点
            processCoursePoints(task.getLoginAccount(), task, log, readyCourse, pointList);

        } catch (Exception e) {
            handleException(task, log, e);
        }
    }

    /**
     * 获取课程信息
     */
    private Course getReadyCourse(SuperStarTask task, SuperStarLog log) throws IOException {
        if (task.getCourseId() != null) {
            log.setCourseId(task.getCourseId());
            return courseUtil.getCourseList(task.getLoginAccount()).stream()
                    .filter(course -> course.getCourseId().equals(task.getCourseId()))
                    .findFirst().orElse(null);
        } else if (task.getCourseName() != null) {
            List<Course> courseList = courseUtil.getCourseList(task.getLoginAccount());
            return Optional.ofNullable(courseList)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(course -> {
                        String dbTitle = StringUtils.normalizeSpace(course.getTitle());
                        String taskName = StringUtils.normalizeSpace(task.getCourseName());
                        return dbTitle.equalsIgnoreCase(taskName);
                    })
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 处理课程点
     */
    private void processCoursePoints(String account, SuperStarTask task, SuperStarLog superStarLog,
                                     Course readyCourse, List<CoursePoint> pointList) {
        try {
            if (pointList == null || pointList.isEmpty()) {
                throw new RuntimeException("章节点信息为空，账号:" + account + "课程:" + readyCourse.getTitle());
            }

            task.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
            this.updateById(task);

            int pointIndex = 0;
            List<ChapterPoint> chapterPointList = pointList.get(0).getPoints();

            if (chapterPointList == null || chapterPointList.isEmpty()) {
                throw new RuntimeException("章节列表为空，账号:" + account + "课程:" + readyCourse.getTitle());
            }

            processAllChapters(account, task, superStarLog, readyCourse, chapterPointList, pointIndex);

        } catch (Exception e) {
            recordErrorLog(task, superStarLog, e, "处理课程点", "course=" + readyCourse.getTitle());
        }
    }

    /**
     * 处理所有章节
     */
    private void processAllChapters(String account, SuperStarTask task, SuperStarLog superStarLog,
                                    Course readyCourse, List<ChapterPoint> chapterPointList, int startIndex) throws NoSuchAlgorithmException, IOException {
        String resumeJobName = superStarLog.getCurrentJob(); // 需要恢复的job名称
        boolean foundResumePoint = (resumeJobName == null);

        for (int pointIndex = startIndex; pointIndex < chapterPointList.size(); pointIndex++) {
            if (Thread.currentThread().isInterrupted() || task.getStatus().equals(WkTaskStatusEnum.PAUSED.getCode())) {
                break;
            }

            ChapterPoint chapterPoint = chapterPointList.get(pointIndex);
            superStarLog.setCurrentChapterIndex(pointIndex);

            try {
                Pair<List<Job>, List<JobInfo>> result = courseUtil.getJobList(
                        account,
                        readyCourse.getClazzId().toString(),
                        readyCourse.getCourseId(),
                        readyCourse.getCpi(),
                        chapterPoint.getId()
                );

                List<Job> jobs = result.getFirst();
                List<JobInfo> jobInfo = result.getSecond();

                if (!foundResumePoint) {
                    boolean jobFoundInThisChapter = false;
                    for (Job job : jobs) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                        String jobName = job.getName();
                        String jobTitle = job.getTitle();
                        if ((jobName != null && jobName.contains(resumeJobName)) ||
                                (jobTitle != null && jobTitle.contains(resumeJobName)) ||
                                pointIndex == superStarLog.getCurrentChapterIndex()) {
                            jobFoundInThisChapter = true;
                            foundResumePoint = true;
                            break;
                        }
                    }


                    if (!jobFoundInThisChapter) {
                        continue;
                    }
                }

                // 检查是否未开放
                if (jobs.stream().findFirst()
                        .map(job -> job instanceof Map && Boolean.TRUE.equals(((Map<?, ?>) job).get("notOpen")))
                        .orElse(false)) {
                    pointIndex--;
                    rb.addTimes(chapterPoint.getId());
                    continue;
                }

                processAllJobs(account, task, superStarLog, readyCourse, jobs, jobInfo, chapterPointList, pointIndex);

            } catch (Exception e) {
                recordErrorLog(task, superStarLog, e, "处理章节", "chapter=" + chapterPoint.getTitle());
            }
        }

        SuperStarTask latestTask = getById(task.getSubOrderId());
        if (!Thread.currentThread().isInterrupted()
                && latestTask != null
                && !latestTask.getStatus().equals(WkTaskStatusEnum.ABNORMAL.getCode())
                && !latestTask.getStatus().equals(WkTaskStatusEnum.PAUSED.getCode())) {
            completeTask(task, superStarLog, readyCourse, account, chapterPointList.size());
        }
    }

    /**
     * 处理所有任务
     */
    private void processAllJobs(String account, SuperStarTask task, SuperStarLog superStarLog,
                                Course readyCourse, List<Job> jobs, List<JobInfo> jobInfo,
                                List<ChapterPoint> chapterPointList, int pointIndex) {
        for (Job job : jobs) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            superStarLog.setId(IdWorker.getId());
            superStarLog.setStartTime(LocalDateTime.now());
            superStarLog.setCurrentChapterIndex(pointIndex);
            superStarLog.setRemark(null);
            superStarLog.setCurrentJob(job.getName());

            try {
                processSingleJob(account, readyCourse, job, jobInfo.get(0), superStarLog);
            } catch (Exception e) {
                recordErrorLog(task, superStarLog, e, "处理任务点", "job=" + job.getName());
            } finally {
                superStarLog.setEndTime(LocalDateTime.now());
                superStarLog.setRemark("任务完成: " + job.getName());
                SuperStarTask latestSuperStarTask = getById(task.getSubOrderId());
                if (latestSuperStarTask != null && latestSuperStarTask.getStatus().equals(WkTaskStatusEnum.PAUSED.getCode())) {
                    superStarLog.setStatus(WkTaskStatusEnum.PAUSED.getCode());
                    try {
                        taskManager.cancelTask(task.getSubOrderId());
                        superStarLog.setRemark(BizCodeEnum.TASK_PAUSED.getMsg());
                    } catch (Exception e) {
                        recordErrorLog(task, superStarLog, e, "任务暂停", "job=" + job.getName());
                    }
                }
            }

            updateProgressAndSaveLog(superStarLog, chapterPointList, pointIndex);
            superStarLogService.saveLog(superStarLog);
        }
    }

    /**
     * 处理单个作业
     */
    private void processSingleJob(String account, Course readyCourse, Job job, JobInfo jobInfo, SuperStarLog superStarLog) throws Exception {
        switch (job.getType()) {
            case JobTypeConstant.VIDEO:
                handleVideoJob(account, readyCourse, job, jobInfo, superStarLog);
                break;
            case JobTypeConstant.DOCUMENT:
                documentAndReadUtil.studyDocument(account, readyCourse, job);
                break;
            case JobTypeConstant.READ:
                documentAndReadUtil.studyRead(account, readyCourse, job, jobInfo, superStarLog);
                break;
            case JobTypeConstant.QUESTION:
//                workUtil.studyWork(account, readyCourse, job, jobInfo, superStarLog);
                break;
            default:
                log.warn("未知作业类型: {}", job.getType());
                break;
        }
    }

    /**
     * 处理视频作业
     */
    private void handleVideoJob(String account, Course readyCourse, Job job, JobInfo jobInfo, SuperStarLog superStarLog) {
        boolean isAudio = false;
        try {
            videoUtil.studyVideo(account, readyCourse, job, jobInfo, studyProperties.getSpeed(), "Video", superStarLog);
        } catch (Exception e) {
            recordErrorLog(null, superStarLog, e, "视频任务", "job=" + job.getName());
            isAudio = true;
        }

        if (isAudio) {
            try {
                videoUtil.studyVideo(account, readyCourse, job, jobInfo, studyProperties.getSpeed(), "Audio", superStarLog);
            } catch (Exception e) {
                recordErrorLog(null, superStarLog, e, "音频任务", "job=" + job.getName());
            }
        }
    }


    /**
     * 更新进度并保存日志
     */
    private void updateProgressAndSaveLog(SuperStarLog superStarLog, List<ChapterPoint> chapterPointList, int pointIndex) {
        int totalChapters = chapterPointList.size();
        double currentPercentage = (pointIndex * 100.0) / totalChapters;
        superStarLog.setCurrentChapterIndex(pointIndex);
        superStarLog.setCurrentProgress(BigDecimal.valueOf(currentPercentage));
    }

    /**
     * 完成任务
     */
    private void completeTask(SuperStarTask task, SuperStarLog superStarLog, Course readyCourse,
                              String account, int totalChapters) throws NoSuchAlgorithmException, IOException {
        superStarLog.setCurrentChapterIndex(totalChapters - 1);
        superStarLog.setCurrentProgress(BigDecimal.valueOf(100));
        superStarLog.setStatus(WkTaskStatusEnum.FINISHED.getCode());

        task.setStatus(WkTaskStatusEnum.FINISHED.getCode());
        updateSuperStarTask(task);

        String score = courseUtil.getScoreFromCourse(account, readyCourse.getClazzId(),
                readyCourse.getCourseId(), readyCourse.getCpi());
        task.setScore(new BigDecimal(score));
        GlobalCookieManager.clearAccount(account);
        superStarLogService.saveLog(superStarLog);

        taskManager.cleanupCompletedTasks();
    }

    /**
     * 统一记录异常日志
     */
    private void recordErrorLog(SuperStarTask task, SuperStarLog log, Exception e,
                                String stage, String context) {
        String shortMsg = Optional.ofNullable(e.getMessage())
                .map(msg -> msg.length() > 40 ? msg.substring(0, 40) : msg)
                .orElse("未知异常");

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String fullStack = sw.toString();

        log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
        log.setErrorMessage(shortMsg);
        log.setRemark("阶段: " + stage + " | 上下文: " + context);
        log.setEndTime(LocalDateTime.now());

        try {
            log.getClass().getMethod("setDetailMessage", String.class).invoke(log, fullStack);
        } catch (Exception ignore) {
        }

        if (task != null) {
            task.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
            this.updateById(task);
        }

        superStarLogService.saveLog(log);

        this.log.error("任务执行异常 | 阶段={} | 上下文={} | 错误={}",
                stage, context, shortMsg, e);
    }

    /**
     * 统一异常处理入口
     */
    private void handleException(SuperStarTask task, SuperStarLog log, Exception e) {
        recordErrorLog(task, log, e, "任务执行", "subOrderId=" + task.getSubOrderId());
    }

}