package com.tihai.utils;

import com.tihai.common.Job;
import com.tihai.common.JobInfo;
import com.tihai.config.OkHttpClientInit;
import com.tihai.domain.chaoxing.SuperStarLog;
import com.tihai.dubbo.pojo.course.Course;
import com.tihai.helper.OkHttpSafetyHelper;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tihai.common.BaseUrl.DOCUMENT_URL;
import static com.tihai.common.BaseUrl.READ_URL;
import static com.tihai.common.ResponseCode.SUCCESS;
import static com.tihai.utils.CommonUtil.getTimestamp;

/**
 * @Copyright : DuanInnovator
 * @Description : 文档工具类
 * @Author : DuanInnovator
 * @CreateTime : 2025/9/21
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
@SuppressWarnings("all")
public class DocumentAndReadUtil {

    /**
     * 文档任务
     *
     * @param account 账号信息
     * @param course  课程信息
     * @param job     任务信息
     * @throws IOException 异常
     */
    public void studyDocument(String account, Course course, Job job) throws IOException {
        String otherInfo = job.getOtherInfo();
        Pattern pattern = Pattern.compile("nodeId_(.*?)-");
        Matcher matcher = pattern.matcher(otherInfo);
        String knowledgeId = "";
        if (matcher.find()) {
            knowledgeId = matcher.group(1);
        }
        String url = String.format(DOCUMENT_URL, job.getJobId(), knowledgeId, course.getCourseId(), course.getClazzId(), job.getJToken(), getTimestamp());
        Request request = new Request.Builder().url(url).build();
        try (OkHttpSafetyHelper.SafeResponse videoInfoResponse = OkHttpSafetyHelper.executeAndReturnSafeResponse(
                OkHttpClientInit.getClient(account, false, false), request)) {
        }

    }

    /**
     * 月度任务
     * @param account 账号信息
     * @param course 课程信息
     * @param job 任务信息
     * @param jobInfo 任务详情信息
     * @param superStarLog 日志
     * @throws IOException
     */
    public void studyRead(String account, Course course, Job job, JobInfo jobInfo, SuperStarLog superStarLog)
            throws IOException {
        HttpUrl url = HttpUrl.parse(READ_URL).newBuilder()
                .addQueryParameter("jobid", job.getJobId())
                .addQueryParameter("knowledgeid", jobInfo.getKnowledgeid())
                .addQueryParameter("jtoken", job.getJToken())
                .addQueryParameter("courseid", course.getCourseId())
                .addQueryParameter("clazzid", course.getClazzId())
                .build();
        Request request = new Request.Builder().url(url).build();
        try (OkHttpSafetyHelper.SafeResponse response = OkHttpSafetyHelper.executeAndReturnSafeResponse(
                OkHttpClientInit.getClient(account, false, false), request)) {

            JSONObject jsonContent = new JSONObject(response.getRawResponse().body().string());
            if (jsonContent.get("code").equals(SUCCESS)) {
                superStarLog.setRemark("阅读任务学习失败 -> [" + jsonContent.get("code") + "]" + response.getRawResponse().body().string());
            } else {
                superStarLog.setRemark("阅读任务学习 -> " + jsonContent.get("msg"));
            }

        }

        superStarLog.setEndTime(LocalDateTime.now());

    }
}

