package com.tihai.utils;

import com.tihai.common.*;
import com.tihai.config.OkHttpClientInit;
import com.tihai.dubbo.pojo.course.Course;
import com.tihai.helper.OkHttpSafetyHelper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.Map;

import static com.tihai.common.BaseUrl.*;

/**
 * @Copyright : DuanInnovator
 * @Description : 课程工具
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
@Slf4j
@SuppressWarnings("all")
public class CourseUtil {


    @Autowired
    private ScoreUtil score;


    /**
     * 获取课程列表请求头
     *
     * @return 请求头
     */
    private Map<String, String> getCourseListHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "mooc2-ans.chaoxing.com");
        headers.put("sec-ch-ua-platform", "\"Windows\"");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ...");
        headers.put("Accept", "text/html, */*; q=0.01");
        headers.put("sec-ch-ua", "\"Microsoft Edge\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("Origin", "https://mooc2-ans.chaoxing.com");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Referer", "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/interaction?moocDomain=https://mooc1-1.chaoxing.com/mooc-ans");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6,ja;q=0.5");
        return headers;
    }


    /**
     * 获取课程列表
     */
    public List<Course> getCourseList(String account) throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("courseType", "1")
                .add("courseFolderId", "0")
                .add("QueryUtil", "")
                .add("superstarClass", "0")
                .build();
        Request request = new Request.Builder()
                .url(COURSE_LIST_URL)
                .post(formBody)
                .headers(Headers.of(getCourseListHeaders()))
                .build();
        try (OkHttpSafetyHelper.SafeResponse courseInfoResponse = OkHttpSafetyHelper.executeAndReturnSafeResponse(OkHttpClientInit.getClient(account, false, false), request)) {
            String respText = courseInfoResponse.getRawResponse().body().string();
            JSONObject jsonContent = null;
            try {
                jsonContent = new JSONObject(respText);
                String error = jsonContent.optString("error");
                if (error.equals("invalid_verify")) {
                    log.error("触发验证码验证-获取课程");
                }
            } catch (JSONException e) {
                //TODO 这里又可能返回的不是json格式
            }


            List<Course> courseList = DecodeUtil.decodeCourseList(respText);

            Request interRequest = new Request.Builder().url(COURSE_FILE_URL).build();
            try (OkHttpSafetyHelper.SafeResponse interResponse = OkHttpSafetyHelper.executeAndReturnSafeResponse(OkHttpClientInit.getClient(account, false, false), interRequest)) {
                String interText = interResponse.getRawResponse().body().string();
                List<CourseFolder> courseFolder = DecodeUtil.decodeCourseFolder(interText);
                for (CourseFolder folder : courseFolder) {
                    FormBody folderForm = new FormBody.Builder()
                            .add("courseType", "1")
                            .add("courseFolderId", folder.getId())
                            .add("QueryUtil", "")
                            .add("superstarClass", "0")
                            .build();
                    Request folderRequest = new Request.Builder()
                            .url(COURSE_LIST_URL)
                            .post(folderForm)
                            .build();

                    try (OkHttpSafetyHelper.SafeResponse folderResponse = OkHttpSafetyHelper.executeAndReturnSafeResponse(OkHttpClientInit.getClient(account, false, false), folderRequest)) {
                        String folderText = folderResponse.getRawResponse().body().string();

                        courseList.addAll(DecodeUtil.decodeCourseList(folderText));
                    }
                }


                return courseList;
            }
        }

    }


    /**
     * 获取课程章节（course point）
     */
    public List<CoursePoint> getCoursePoint(String account, String courseId, String clazzId, String cpi) throws IOException {
        String url = String.format(COURSE_POINT_URL,
                courseId, clazzId, cpi);
        Request request = new Request.Builder().url(url).build();

        try (OkHttpSafetyHelper.SafeResponse response = OkHttpSafetyHelper.executeAndReturnSafeResponse(OkHttpClientInit.getClient(account, false, false), request)) {
            String respText = response.getRawResponse().body().string();

            try {
                JSONObject jsonContent = new JSONObject(respText);
                String error = jsonContent.optString("error");
                if (error.equals("invalid_verify")) {
                    log.error("触发验证码验证-获取课程章节");
                }
            } catch (JSONException e) {

            } finally {
                CoursePoint stringObjectMap = DecodeUtil.decodeCoursePoint(respText);
                return Arrays.asList(DecodeUtil.decodeCoursePoint(respText));
            }

        }


    }

    /**
     * 获取任务点列表
     */
    public Pair<List<Job>, List<JobInfo>> getJobList(String account, String clazzId, String courseId, String cpi, String knowledgeId) throws IOException {
        List<Job> jobList = new ArrayList<>();
        List<JobInfo> jobInfo = new ArrayList<>();
        String[] possibleNums = {"0", "1", "2"};
        for (String num : possibleNums) {
            String url = String.format(JOB_LIST_URL,
                    clazzId, courseId, knowledgeId, num, cpi);
            Request request = new Request.Builder().url(url).build();

            try (OkHttpSafetyHelper.SafeResponse response = OkHttpSafetyHelper.executeAndReturnSafeResponse(OkHttpClientInit.getClient(account, false, false), request)) {
                String respText = response.getRawResponse().body().string();
                Pair<List<Job>, JobInfo> result = DecodeUtil.decodeCourseCard(respText);
                List<Job> _jobList = result.getFirst();

                List<JobInfo> _jobInfo = Arrays.asList(result.getSecond());
                if (Boolean.TRUE.equals(_jobInfo.get(0).getNotOpen())) {
                    return new Pair<>(new ArrayList<>(), _jobInfo);
                }
                jobList.addAll(_jobList);
                jobInfo.addAll(_jobInfo);
            }


        }
        return new Pair<>(jobList, jobInfo);
    }


    /**
     * 获取课程分数
     *
     * @param clazzId  clazzId
     * @param courseId courseId
     * @param cpi      cpi
     * @return 分数
     * @throws IOException 异常
     */
    public String getScoreFromCourse(String account, String clazzId, String courseId, String cpi) throws
            NoSuchAlgorithmException, IOException {

        String url = String.format(SCORE_URL, courseId, clazzId, cpi);
        String originHtmlContent = "";
        Request request = new Request.Builder().url(url).build();
        try (OkHttpSafetyHelper.SafeResponse response = OkHttpSafetyHelper.executeAndReturnSafeResponse(OkHttpClientInit.getClient(account, false, false), request)) {
            originHtmlContent = response.getRawResponse().body().string();
            return score.getScoreFromCourse(originHtmlContent);
        }

    }
}

