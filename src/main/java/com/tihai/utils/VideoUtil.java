package com.tihai.utils;

import com.tihai.common.Job;
import com.tihai.common.JobInfo;
import com.tihai.config.OkHttpClientInit;
import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.dubbo.pojo.course.Course;
import com.tihai.helper.OkHttpSafetyHelper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.tihai.common.BaseUrl.*;
import static com.tihai.utils.CommonUtil.*;

/**
 * @Copyright : DuanInnovator
 * @Description : 视频工具类\
 * @Author : DuanInnovator
 * @CreateTime : 2025/9/21
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
@SuppressWarnings("all")
@Slf4j
public class VideoUtil {

    /**
     * 视频播放进度日志
     */
    public boolean videoProgressLog(String account, Course course,
                                    Job job, JobInfo jobInfo, String dtoken, long duration,
                                    long playingTime, String type) throws Exception {

        final String[] possibleRts = {"0.9", "1"};
        boolean success = false;
        JSONObject jsonContent = new JSONObject();
        String respText;
        final String uid = getValue("_uid");
        final String clazzId = course.getClazzId();
        final String jobId = job.getJobId();
        final String objectId = job.getObjectId();
        final String midText = job.getOtherInfo().contains("courseId") ?
                "otherInfo=" + job.getOtherInfo() + "&" :
                "otherInfo=" + job.getOtherInfo() + "&courseId=" + course.getCourseId() + "&";
        for (String possibleRt : possibleRts) {

            final String timestamp = getTimestamp();
            final String encValue = getEnc(clazzId, jobId, objectId, playingTime, duration, uid);
            StringBuilder urlBuilder = new StringBuilder(350);
            urlBuilder.append(VIDEO_PROCESS_LOG_BASE_URL)
                    .append(course.getCpi()).append("/")
                    .append(dtoken).append("?clazzId=")
                    .append(clazzId).append("&playingTime=")
                    .append(playingTime).append("&duration=")
                    .append(duration).append("&clipTime=0_")
                    .append(duration).append("&objectId=")
                    .append(objectId).append("&")
                    .append(midText).append("jobid=")
                    .append(jobId).append("&userid=")
                    .append(uid).append("&isdrag=3&view=pc&enc=")
                    .append(encValue).append("&rt=")
                    .append(possibleRt).append("&dtype=")
                    .append(type).append("&_t=")
                    .append(timestamp);

            Request request = new Request.Builder().url(urlBuilder.toString()).build();


            try (OkHttpSafetyHelper.SafeResponse videoInfoResponse = OkHttpSafetyHelper.executeAndReturnSafeResponse(
                    OkHttpClientInit.getClient(account, "Video".equals(type), "Audio".equals(type)), request)) {
                respText = videoInfoResponse.getRawResponse().body().string();
                jsonContent = new JSONObject(respText);
                String error = jsonContent.optString("error");
                if (error.equals("invalid_verify")) {
                    log.error("触发验证码验证-课程视频学习");
                }
                if (jsonContent.get("isPassed") != null) {
                    success = true;
                    break;
                }

            } catch (IOException exception) {
                continue;
            } catch (Exception exception) {
                log.error("视频任务异常: -> " + exception.getMessage());
                throw exception;
            }

        }
        return success && (jsonContent != null && jsonContent.has("isPassed") && jsonContent.getBoolean("isPassed"));

    }

    /**
     * 视频学习
     *
     * @param account
     * @param course
     * @param job
     * @param jobInfo
     * @param speed
     * @param type
     * @param superStarLog
     * @throws Exception
     */
    public void studyVideo(String account, Course course, Job job, JobInfo jobInfo,
                           double speed, String type, SuperStarLog superStarLog) throws Exception {

        String infoUrl = String.format(VIDEO_STATUS_URL,
                job.getObjectId(), getValue("fid"));
        Request infoRequest = new Request.Builder().url(infoUrl).build();

        try (OkHttpSafetyHelper.SafeResponse videoInfoResponse = OkHttpSafetyHelper.executeAndReturnSafeResponse(
                OkHttpClientInit.getClient(account, "Video".equals(type), "Audio".equals(type)),
                infoRequest)) {
            JSONObject jsonContent = new JSONObject(videoInfoResponse.getRawResponse().body().string());
            if ("success".equals(jsonContent.get("status"))) {
                String dtoken = jsonContent.get("dtoken").toString();
                long duration = Long.parseLong(jsonContent.get("duration").toString());
                long playingTime = 0;
                int retryCount = 0;
                final int maxRetries = 3;
                final int retryInterval = 10000;
                final int fixedWaitSeconds = 60;

                log.info("开始学习视频: 课程名:{} 任务名:{} 总时长:{}秒",
                        course.getTitle(), job.getName(), duration);

                while (playingTime < duration) {
                    boolean progress = videoProgressLog(account, course, job, jobInfo,
                            dtoken, duration, playingTime, type);

                    if (progress) {
                        return;
                    }

                    int waitTime = Math.min((int) (fixedWaitSeconds * speed),
                            (int) (duration - playingTime));

                    if (waitTime > 0) {
                        Thread.sleep(Math.min(waitTime * 1000, 60000));
                    }

                    playingTime += waitTime;
                }

                log.warn("播放时间已超过视频时长，开始重试检测: 课程名:{} 任务名:{}",
                        course.getTitle(), job.getName());

                while (retryCount < maxRetries) {
                    boolean progress = videoProgressLog(account, course, job, jobInfo,
                            dtoken, duration, playingTime, type);

                    if (progress) {
                        log.info("重试成功: 视频学习完成: 课程名:{} 任务名:{}",
                                course.getTitle(), job.getName());
                        return;
                    }

                    retryCount++;
                    log.warn("第{}次重试失败，等待{}秒后再次尝试", retryCount, retryInterval / 1000);
                    Thread.sleep(retryInterval);
                }

            }
        }
    }

}

