package com.tihai.service.superstar.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.tihai.factory.CustomThreadFactory;
import com.tihai.factory.PriorityRejectPolicy;
import com.tihai.manager.RollBackManager;
import com.tihai.mapper.SuperStarMapper;
import com.tihai.properties.StudyProperties;
import com.tihai.properties.ThreadPoolProperties;
import com.tihai.queue.PriorityTaskWrapper;
import com.tihai.service.nacos.NacosInstanceService;
import com.tihai.service.superstar.SuperStarLogService;
import com.tihai.service.superstar.SuperStarLoginService;
import com.tihai.service.superstar.SuperStarTaskService;
import com.tihai.service.superstar.SuperStarUserService;
import com.tihai.utils.CourseUtil;
import com.tihai.utils.ServerInfoUtil;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;


/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-任务启动和实现
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/26
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
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

   @Autowired
   private ServerInfoUtil serverInfoUtil;

    @Autowired
     private ThreadPoolProperties config;

    @Autowired
    private StudyProperties studyProperties;


    private volatile boolean isShuttingDown = false;
    private final Object shutdownLock = new Object();


    // 日志批量保存的缓冲队列
    private final BlockingQueue<SuperStarLog> logQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<Long, SuperStarLog> logMap = new ConcurrentHashMap<>();

    public SuperStarTaskServiceImpl() throws NacosException {
    }

    public void shutdownThreadPoolGracefully() throws NacosException {
        // 提前检查减少同步块竞争
        if (isShuttingDown || taskExecutor.isShutdown()) {
            return;
        }

        synchronized (shutdownLock) {
            // 再次检查（双重检查锁模式）
            if (isShuttingDown || taskExecutor.isShutdown()) {
                return;
            }
            isShuttingDown = true;

            try {
                // 1. 停止接受新任务
                taskExecutor.shutdown();
                log.info("线程池开始关闭，等待现有任务完成...");

                // 2. 分阶段等待
                // 第一阶段：等待正常完成
                if (!taskExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
                    log.warn("等待超时，尝试强制停止剩余任务...");

                    // 第二阶段：强制停止
                    List<Runnable> unfinishedTasks = taskExecutor.shutdownNow();

                    // 3. 处理未完成任务
                    if (!unfinishedTasks.isEmpty()) {
                        log.warn("有{}个任务被强制停止，开始回滚状态...", unfinishedTasks.size());
                        processUnfinishedTasks(unfinishedTasks);
                    }

                    // 最后检查
                    if (!taskExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
                        log.error("线程池最终未能完全关闭");
                    }
                }
            } catch (InterruptedException e) {
                // 处理中断
                List<Runnable> unfinishedTasks = taskExecutor.shutdownNow();
                if (!unfinishedTasks.isEmpty()) {
                    processUnfinishedTasks(unfinishedTasks);
                }
                Thread.currentThread().interrupt();
                throw new NacosException(NacosException.SERVER_ERROR, "线程池关闭被中断");
            } finally {
                log.info("线程池关闭完成，当前状态: isShutdown={}, isTerminated={}",
                        taskExecutor.isShutdown(), taskExecutor.isTerminated());

            }
        }
    }

    private void processUnfinishedTasks(List<Runnable> unfinishedTasks) {
        unfinishedTasks.forEach(task -> {
            if (task instanceof PriorityTaskWrapper) {
                String taskId = ((PriorityTaskWrapper) task).getTaskId();
                try {
                    SuperStarTask dbTask = this.getById(taskId);
                    if (dbTask != null &&
                            (WkTaskStatusEnum.PROCESSING.getCode().equals(dbTask.getStatus()) ||
                                    WkTaskStatusEnum.EXAM.getCode().equals(dbTask.getStatus()))) {
                        dbTask.setStatus(WkTaskStatusEnum.PAUSED.getCode());
                        this.updateById(dbTask);
                    }
                } catch (Exception e) {
                    log.error("回滚任务状态失败, taskId={}", taskId, e);
                }
            }
        });
    }
    public void restartThreadPoolAndTasks() {
        synchronized (shutdownLock) {
            if (!taskExecutor.isShutdown()) {
                return;
            }

            BlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(config.getQueueCapacity());

            // 重新构建线程池
            taskExecutor = new ThreadPoolExecutor(
                    config.getCoreSize(),
                    config.getMaxSize(),
                    config.getKeepAlive(),
                    TimeUnit.SECONDS,
                    queue,
                    new CustomThreadFactory(config.getThreadNamePrefix()),
                    new PriorityRejectPolicy()
            );
            taskExecutor.allowCoreThreadTimeOut(config.isAllowCoreThreadTimeout());
            this.startChaoxingTask();
        }
    }

    @PostConstruct
    public void init() {
        // 启动一个单独的线程来处理日志的批量保存
        new Thread(this::batchSaveLogs).start();
    }

    private void batchSaveLogs() {
        List<SuperStarLog> batch = new ArrayList<>(100);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SuperStarLog log = logQueue.poll(1, TimeUnit.SECONDS);
                if (log != null) {
                    batch.add(log);
                    logMap.put(log.getId(), log); // 合并更新
                }
                // 批量保存条件：达到100条或超时
                if (batch.size() >= 100 || (log == null && !batch.isEmpty())) {
                    List<SuperStarLog> toSave = new ArrayList<>(logMap.values());

                    if (log != null) {
                        log.setMachineNum(serverInfoUtil.getCurrentServerInstance());
                    }
                    superStarLogService.saveOrUpdateBatch(toSave);
                    logMap.clear();
                    batch.clear();

                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (NacosException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private SuperStarLog createNewLog(SuperStarTask task) {
        SuperStarLog log = mapperFacade.map(task, SuperStarLog.class);
        log.setStartTime(LocalDateTime.now());
        log.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
        // 首次插入，获取自增ID
        superStarLogService.save(log);
        return log;
    }

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
     * 添加任务到等待队列
     *
     * @param courseSubmitTaskDTO 任务信息
     * @throws NacosException nacos异常
     */
    public void addChaoXingTask(CourseSubmitTaskDTO courseSubmitTaskDTO) throws NacosException {
        SuperStarTask task=null;
        if(courseSubmitTaskDTO.getCourseId()==null){
            task = getSuperStarTaskByCourseName(courseSubmitTaskDTO.getLoginAccount(), courseSubmitTaskDTO.getCourseName());
        }else {
            task = getSuperStarTask(courseSubmitTaskDTO.getLoginAccount(), courseSubmitTaskDTO.getCourseId());
        }
        if (task == null) {
            SuperStarTask chaoXingTask = mapperFacade.map(courseSubmitTaskDTO, SuperStarTask.class);
            chaoXingTask.setStatus(WkTaskStatusEnum.PENDING.getCode());
            chaoXingTask.setId(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))); //当前version使用UUID
            chaoXingTask.setPriority(1);
            chaoXingTask.setCreatTime(LocalDateTime.now());
            chaoXingTask.setMachineNum(serverInfoUtil.getCurrentServerInstance());
            this.baseMapper.insert(chaoXingTask);
        } else {
            throw new BusinessException(BizCodeEnum.TASK_ALREADY_EXIST.getCode(), BizCodeEnum.TASK_ALREADY_EXIST.getMsg());
        }
        //TODO 这里似乎不太合理,等待后续优化.......
//        startChaoxingTask();
    }

    /**
     * 超星学习任务启动
     */
    @Override
    public void startChaoxingTask() {
        LambdaQueryWrapper<SuperStarTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SuperStarTask::getStatus, WkTaskStatusEnum.PENDING.getCode());
        wrapper.le(SuperStarTask::getRetryCount, RetryConstant.DEFAULT_RETRY_COUNT);
        wrapper.orderByDesc(SuperStarTask::getPriority);
        List<SuperStarTask> tasks = list(wrapper);
//        executeCourseTask(tasks.get(0));

        tasks.forEach(task -> {
            task.setStatus(WkTaskStatusEnum.QUEUE.getCode());
            try {
                task.setMachineNum(serverInfoUtil.getCurrentServerInstance());
            } catch (NacosException e) {
                throw new RuntimeException(e);
            }
            boolean isUpdated = this.updateById(task);
            Runnable taskWrapper = new PriorityTaskWrapper(() -> {
                try {
                    executeCourseTask(task);
                } catch (Exception e) {
                    if (task.getRetryCount() == RetryConstant.DEFAULT_RETRY_COUNT) {
                        task.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                    } else {
                        task.setRetryCount(task.getRetryCount() + 1);
                        task.setStatus(WkTaskStatusEnum.PENDING.getCode());
                    }
                    this.updateById(task);
                }
            }, task.getPriority(), task.getId());

            taskExecutor.execute(taskWrapper);
        });
    }

    /**
     * 执行超星学习任务
     *
     * @param task 超星学习任务
     */
    public void executeCourseTask(SuperStarTask task) {
        WkUser user = userService.getUserByAccount(task.getLoginAccount());
        SuperStarLog log = superStarLogService.getLatestLogByLoginAccount(task.getLoginAccount(), task.getCourseName());

        if (log == null) {
            log = createNewLog(task);
        }else{
            log.setStartTime(LocalDateTime.now());
        }


        if (user == null || user.getCookies() == null) {
            if (task.getRetryCount() < RetryConstant.DEFAULT_RETRY_COUNT) {
                task.setStatus(WkTaskStatusEnum.PENDING.getCode());
                task.setRetryCount(task.getRetryCount() + 1);
            } else {
                task.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
            }
            WkUser wkUser = new WkUser();
            wkUser.setAccount(task.getLoginAccount());
            wkUser.setPassword(task.getPassword());
            loginService.login(wkUser, true);
            this.updateById(task);
            log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
            log.setErrorMessage("用户未登录");
            logQueue.offer(log);
            return;
        }

        try {
            Course readyCourse=new Course();
            courseUtil.setAccount(task.getLoginAccount());
            courseUtil.setCookies(user.getCookies());
            if(task.getCourseId()!=null){
                readyCourse = courseUtil.getCourseList().stream()
                        .filter(course -> course.getCourseId().equals(task.getCourseId()))
                        .findFirst().orElse(null);
            }else if(task.getCourseName()!=null){
                readyCourse = courseUtil.getCourseList().stream()
                        .filter(course -> course.getTitle().equals(task.getCourseName()))
                        .findFirst().orElse(null);
            }

            if (readyCourse == null) {
                loginService.login(user,true);
                log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                log.setErrorMessage(GlobalConstant.COURSE_INFO_GET_FAIL);
                logQueue.offer(log);
                return;
            }

            List<CoursePoint> pointList = courseUtil.getCoursePoint(
                    readyCourse.getCourseId(), readyCourse.getClazzId(), readyCourse.getCpi());

            processCoursePoints(task, log, readyCourse, pointList);
        } catch (Exception e) {
            log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
            log.setErrorMessage("系统异常");
            logQueue.offer(log);
        }
    }

    private void processCoursePoints(SuperStarTask task, SuperStarLog log, Course readyCourse, List<CoursePoint> pointList) {
        if (log == null) {
            log = mapperFacade.map(task, SuperStarLog.class);
            log.setId(Long.valueOf(UUID.randomUUID().toString()));
            log.setStatus(WkTaskStatusEnum.PROCESSING.getCode());
            logQueue.offer(log);
        }

        int pointIndex = log.getCurrentChapterIndex() != null ? log.getCurrentChapterIndex() : 0;
//        System.out.println("当前章节索引：" + pointIndex);
        List<ChapterPoint> chapterPointList = pointList.get(0).getPoints();
//        System.out.println(chapterPointList.size());

        while (pointIndex < chapterPointList.size()) {
//            System.out.println("当前线程状态"+Thread.currentThread().isInterrupted());
//            if (Thread.currentThread().isInterrupted()) { //线程暂停
//                break;
//            }
            try {
                log.setCurrentChapterIndex(pointIndex);
                ChapterPoint chapterPoint = chapterPointList.get(pointIndex);
                Pair<List<Job>, List<JobInfo>> result = courseUtil.getJobList(
                        readyCourse.getClazzId().toString(),
                        readyCourse.getCourseId(),
                        readyCourse.getCpi(),
                        chapterPoint.getId());
//                System.out.println("当前章节作业数量：" + result.toString());

                List<Job> jobs = result.getFirst();
                List<JobInfo> jobInfo = result.getSecond();

                if (jobs.isEmpty()) {
//                    System.out.println("当前章节没有作业");
                    pointIndex++;
                    continue;
                }

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
                                    courseUtil.studyVideo(readyCourse, job, jobInfo.get(0), studyProperties.getSpeed(), "Video", log);
                                } catch (Exception e) {
                                    isAudio = true;
                                }
                                if (isAudio) {
                                    try {
                                        courseUtil.studyVideo(readyCourse, job, jobInfo.get(0), studyProperties.getSpeed(), "Audio", log);
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
                                courseUtil.studyWork(readyCourse, job, jobInfo.get(0),log);
                            default:
                                break;

                        }

                    } catch (Exception e) {
                        log.setEndTime(LocalDateTime.now());
                        log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                        log.setErrorMessage("任务异常，任务ID=" + job.getTitle());
                    }
                }
                pointIndex++;

                int totalChapters = chapterPointList.size();
                double currentPercentage = (pointIndex * 100.0) / totalChapters;
                log.setCurrentChapterIndex(pointIndex - 1);
                log.setEndTime(LocalDateTime.now());
                log.setCurrentProgress(BigDecimal.valueOf(currentPercentage));
                logQueue.offer(log);


            } catch (Exception e) {
                log.setStatus(WkTaskStatusEnum.ABNORMAL.getCode());
                log.setErrorMessage("章节处理异常");
                logQueue.offer(log);
                break;
            }
        }

        if (pointIndex >= pointList.size()) {
            log.setEndTime(LocalDateTime.now());
            log.setCurrentChapterIndex(pointIndex - 1);
            log.setCurrentProgress(BigDecimal.valueOf(100));
            log.setStatus(WkTaskStatusEnum.FINISHED.getCode());

            logQueue.offer(log);
        }
    }
}