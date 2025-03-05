package com.wk.utils;

import com.alibaba.dashscope.aigc.generation.GenerationResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wk.api.QWen;
import com.wk.common.*;
import com.wk.config.GlobalConst;
import com.wk.domain.chaoxing.SuperStarLog;
import com.wk.service.superstar.SuperStarLogService;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Copyright : DuanInnovator
 * @Description : 课程工具
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
public class CourseUtil {

    private static final Gson gson = new Gson();
    public Map<String, String> cookies;
    public List<Cookie> cookieList = new ArrayList<>();
    @Autowired
    private SuperStarLogService superStarLogService;


    public void setCookies(String cookies) {
        this.cookies = convertCookieStringToMap(cookies);
        cookieList = convertCookieStringToList(cookies);
    }

    public static List<Cookie> convertCookieStringToList(String cookieStr) {
        List<Cookie> cookies = new ArrayList<>();
        HttpUrl httpUrl = HttpUrl.parse("chaoxing.com"); // 需要目标网站的 URL

        if (cookieStr == null || cookieStr.isEmpty() ) {
            return cookies;
        }

        String[] cookieArray = cookieStr.split(", "); // 逗号+空格分割多个 Cookie
        for (String cookieEntry : cookieArray) {
            String[] parts = cookieEntry.split(";");  // 分号分割 Cookie 属性
            if (parts.length > 0) {
                String[] keyValue = parts[0].split("=", 2);  // 分割键和值
                if (keyValue.length == 2) {
                    Cookie cookie = new Cookie.Builder()
                            .name(keyValue[0].trim())
                            .value(keyValue[1].trim())
                            .domain("chaoxing.com")  // 你可以改成数据库存的域名
                            .path("/")  // 默认路径
                            .build();
                    cookies.add(cookie);
                }
            }
        }
        return cookies;
    }

    public static Map<String, String> convertCookieStringToMap(String cookieStr) {
        // 去掉前后的方括号
        if (cookieStr.startsWith("[") && cookieStr.endsWith("]")) {
            cookieStr = cookieStr.substring(1, cookieStr.length() - 1);
        }
        Map<String, String> cookieMap = new LinkedHashMap<>();
        String[] cookies = cookieStr.split(", ");


        for (String cookie : cookies) {
            String[] attributes = cookie.split(";");
            String[] keyValue = attributes[0].split("=", 2); // 只取第一个键值对，防止 value 中带 "=" 导致错误

            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // 如果 key 已存在，合并值，确保所有相同 key 的值都能保留
                if (cookieMap.containsKey(key)) {
                    cookieMap.put(key, cookieMap.get(key) + "," + value);
                } else {
                    cookieMap.put(key, value);
                }
            }
        }
        return cookieMap;
    }

    public String getValue(String key) {
        return cookies.get(key);
    }


    // 获取随机等待秒数（30-90秒）
    public static int getRandomSeconds() {
        return ThreadLocalRandom.current().nextInt(100, 120);
    }

    // 获取时间戳（毫秒）
    public static String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }


    /**
     * 初始化 OkHttpClient，并根据 isVideo / isAudio 设置请求头
     */
    public OkHttpClient initSession(boolean isVideo, boolean isAudio) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // 配置重试和 CookieJar（假设 Cookies.useCookies(url) 返回 List<Cookie>）
        builder.retryOnConnectionFailure(true);
        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//                Cookies.saveCookies(url, cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                return cookieList;
            }
        });
        // 使用拦截器添加统一请求头
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder reqBuilder = original.newBuilder();
            Map<String, String> headers;
            if (isVideo) {
                headers = GlobalConst.VIDEO_HEADERS;
            } else if (isAudio) {
                headers = GlobalConst.AUDIO_HEADERS;
            } else {
                headers = GlobalConst.HEADERS;
            }
            headers.forEach(reqBuilder::header);
            Request req = reqBuilder.build();
            return chain.proceed(req);
        });
        return builder.build();
    }


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
    public List<Course> getCourseList() throws IOException {
        OkHttpClient client = initSession(false, false);
//        OkHttpClient client =new OkHttpClient();
        String url = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/courselistdata";

        FormBody formBody = new FormBody.Builder()
                .add("courseType", "1")
                .add("courseFolderId", "0")
                .add("query", "")
                .add("superstarClass", "0")
                .build();

        // 此处定义专用的请求头
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .headers(Headers.of(getCourseListHeaders()))
                .build();



        Response response = client.newCall(request).execute();

        String respText = response.body().string();
        List<Course> courseList = Decode.decodeCourseList(respText);

        // 处理课程文件夹
        String interactionUrl = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/interaction";
        Request interRequest = new Request.Builder().url(interactionUrl).build();
        Response interResponse = client.newCall(interRequest).execute();
        String interText = interResponse.body().string();
        List<CourseFolder> courseFolder = Decode.decodeCourseFolder(interText);
        for (CourseFolder folder : courseFolder) {
            FormBody folderForm = new FormBody.Builder()
                    .add("courseType", "1")
                    .add("courseFolderId", folder.getId())
                    .add("query", "")
                    .add("superstarClass", "0")
                    .build();
            Request folderRequest = new Request.Builder()
                    .url(url)
                    .post(folderForm)
                    .build();
            Response folderResponse = client.newCall(folderRequest).execute();
            String folderText = folderResponse.body().string();
            System.out.println(folderText);
            courseList.addAll(Decode.decodeCourseList(folderText));
        }
        return courseList;
    }


    /**
     * 获取课程章节（course point）
     */
    public List<CoursePoint> getCoursePoint(String courseId, String clazzId, String cpi) throws IOException {
        OkHttpClient client = initSession(false, false);
        String url = String.format("https://mooc2-ans.chaoxing.com/mooc2-ans/mycourse/studentcourse?courseid=%s&clazzid=%s&cpi=%s&ut=s",
                courseId, clazzId, cpi);
//        //loggerUtil.//logger.trace("开始读取课程所有章节...");

        List<Cookie> cookies = Cookies.useCookies();

        Request request = new Request.Builder().url(url).addHeader("Cookie", cookies.toString()).build();
        Response response = client.newCall(request).execute();
        String respText = response.body().string();
//        //loggerUtil.//logger.info("课程章节读取成功...");
        CoursePoint stringObjectMap = Decode.decodeCoursePoint(respText);
        return Arrays.asList(Decode.decodeCoursePoint(respText));
    }

    /**
     * 获取任务点列表
     */
    public Pair<List<Job>, List<JobInfo>> getJobList(String clazzId, String courseId, String cpi, String knowledgeId) throws IOException {
        OkHttpClient client = initSession(false, false);
        List<Job> jobList = new ArrayList<>();
        List<JobInfo> jobInfo = new ArrayList<>();
        String[] possibleNums = {"0", "1", "2"};
        for (String num : possibleNums) {
            String url = String.format("https://mooc1.chaoxing.com/mooc-ans/knowledge/cards?clazzid=%s&courseid=%s&knowledgeid=%s&num=%s&ut=s&cpi=%s&v=20160407-3&mooc2=1",
                    clazzId, courseId, knowledgeId, num, cpi);
            //loggerUtil.//logger.trace("开始读取章节所有任务点...");
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            String respText = response.body().string();
            Pair<List<Job>, JobInfo> result = Decode.decodeCourseCard(respText);
            List<Job> _jobList = result.getFirst();
            List<JobInfo> _jobInfo = Arrays.asList(result.getSecond());
            if (Boolean.TRUE.equals(_jobInfo.get(0).getNotOpen())) {
                //loggerUtil.//logger.info("该章节未开放");
                return new Pair<>(new ArrayList<>(), _jobInfo);
            }
            jobList.addAll(_jobList);
            jobInfo.addAll(_jobInfo);
        }
        //loggerUtil.//logger.info("章节任务点读取成功...");
        return new Pair<>(jobList, jobInfo);
    }

    /**
     * 生成 MD5 加密串
     */
    public String getEnc(String clazzId, String jobId, String objectId, long playingTime, long duration, String userId)
            throws Exception {
        String str = String.format("[%s][%s][%s][%s][%d][d_yHJ!$pdA~5][%d][0_%s]",
                clazzId, userId, jobId, objectId, playingTime * 1000, duration * 1000, duration);
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 视频播放进度日志
     */
    public Boolean videoProgressLog(Course course,
                                    Job job, JobInfo jobInfo, String dtoken, long duration,
                                    long playingTime, String type) throws Exception {
        OkHttpClient client = initSession(false, false);
        String midText;
        String respText = null;
        if (job.getOtherInfo().contains("courseId")) {
            midText = "otherInfo=" + job.getOtherInfo() + "&";
        } else {
            midText = "otherInfo=" + job.getOtherInfo() + "&courseId=" + course.getCourseId() + "&";
        }
        boolean success = false;
        Response response = null;
        for (String possibleRt : new String[]{"0.9", "1"}) {
            String url = String.format("https://mooc1.chaoxing.com/mooc-ans/multimedia/log/a/%s/%s?clazzId=%s&playingTime=%s" +
                            "&duration=%s&clipTime=0_%s&objectId=%s&%sjobid=%s&userid=%s&isdrag=3&view=pc&enc=%s&rt=%s&dtype=%s&_t=%s&attDuration=%s&attDurationEnc=%s",
                    course.getCpi(), dtoken, course.getClazzId(), playingTime, duration, duration,
                    job.getObjectId(),
                    midText, job.getJobId(), getValue("_uid"),
                    getEnc(course.getClazzId(), job.getJobId(), job.getObjectId(),
                            playingTime, duration, getValue("_uid")),
                    possibleRt, type, getTimestamp(), duration, 1);
//            Request request = new Request.Builder().url(url).addHeader("Cookies", this.cookies.toString()).build();
            Request request = new Request.Builder().url(url).build();
            System.out.println(request);
            response = client.newCall(request).execute();
            respText = response.body().string();
//            System.out.println(respText + ":" + respText);
            if (response.code() == 200) {
                success = true;
                break;
            } else if (response.code() == 403) {
                continue;
            }
        }
        if (success && respText != null) {
//            //因为学习通后台数据延迟，实际上只需要判断是否播放完成，所以这里直接返回true
//            if (playingTime >= duration) {
//                return true;
//            }
            return (Boolean) JsonParser.parse(respText).get("isPassed");
        } else {
            //loggerUtil.//logger.error("出现403报错，尝试修复无效，正在跳过当前任务点...");
            return null;
        }
    }

    /**
     * 学习视频（模拟观看）
     */
    public void studyVideo(Course course, Job job, JobInfo jobInfo,
                           double speed, String type, SuperStarLog log) throws Exception {

        OkHttpClient session = ("Video".equals(type)) ?
                initSession(true, false) : initSession(false, true);
        // 获取视频信息
        String infoUrl = String.format("https://mooc1.chaoxing.com/ananas/status/%s?k=%s&flag=normal",
                job.getObjectId(), getValue("fid"));
        Request infoRequest = new Request.Builder().url(infoUrl).build();
        Response infoResponse = session.newCall(infoRequest).execute();
        Map<String, Object> videoInfo = JsonParser.parse(infoResponse.body().string());
        if ("success".equals(videoInfo.get("status"))) {
            String dtoken = videoInfo.get("dtoken").toString();
            long duration = Long.parseLong(videoInfo.get("duration").toString());
            boolean isFinished = false;
            long playingTime = 0;
            System.out.println(Thread.currentThread().getName() + "开始学习视频: " + job.getName());
            log.setRemark("开始任务: " + job.getName() + ", 总时长: " + duration + "秒");
            log.setCurrentJob(job.getName());
            superStarLogService.saveLog(log);
            while (!isFinished) {
                Boolean progress = videoProgressLog(course, job, jobInfo, dtoken, duration,
                        playingTime, type);
                if (progress == null || progress == true) {
                    break;
                }
                int waitTime = (int) (getRandomSeconds() * speed);
                if (playingTime + waitTime >= duration) {
                    waitTime = (int) (duration - playingTime);
                }

                Thread.sleep(60000);

                playingTime += waitTime;
            }
            log.setRemark("任务完成: " + job.getName());
            superStarLogService.saveLog(log);

        }
    }

    /**
     * 学习文档
     */
    public void studyDocument(Course course, Job job) throws IOException {
        OkHttpClient client = initSession(false, false);
        String otherInfo = job.getOtherInfo();
        Pattern pattern = Pattern.compile("nodeId_(.*?)-");
        Matcher matcher = pattern.matcher(otherInfo);
        String knowledgeId = "";
        if (matcher.find()) {
            knowledgeId = matcher.group(1);
        }
        String url = String.format("https://mooc1.chaoxing.com/ananas/job/document?jobid=%s&knowledgeid=%s&courseid=%s&clazzid=%s&jtoken=%s&_dc=%s",
                job.getJobId(), knowledgeId, course.getCourseId(), course.getClazzId(), job.getJToken(), getTimestamp());
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).execute();
    }

    /**
     * 阅读任务，只完成任务点，不增加时长
     */
    public void studyRead(Course course, Job job, JobInfo jobInfo, SuperStarLog log)
            throws IOException {
        OkHttpClient client = initSession(false, false);
        HttpUrl url = HttpUrl.parse("https://mooc1.chaoxing.com/ananas/job/readv2").newBuilder()
                .addQueryParameter("jobid", job.getJobId())
                .addQueryParameter("knowledgeid", jobInfo.getKnowledgeid())
                .addQueryParameter("jtoken", job.getJToken())
                .addQueryParameter("courseid", course.getCourseId())
                .addQueryParameter("clazzid", course.getClazzId())
                .build();
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200) {
            log.setRemark("阅读任务学习失败 -> [" + response.code() + "]" + response.body().string());
        } else {
            Map<String, Object> respJson = JsonParser.parse(response.body().string());
            log.setRemark("阅读任务学习 -> " + respJson.get("msg"));
        }
        superStarLogService.saveLog(log);
    }

    public void studyWork(Course course, Job job, JobInfo jobInfo) throws IOException {

        OkHttpClient client = initSession(false, false);
        String originHtmlContent = "";

        // 获取作业页面
        String url = "https://mooc1.chaoxing.com/mooc-ans/api/work";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder()
                .addQueryParameter("api", "1")
                .addQueryParameter("workId", job.getJobId().replace("work-", ""))
                .addQueryParameter("jobid", job.getJobId())
                .addQueryParameter("originJobId", job.getJobId())
                .addQueryParameter("needRedirect", "true")
                .addQueryParameter("skipHeader", "true")
                .addQueryParameter("knowledgeid", jobInfo.getKnowledgeid())
                .addQueryParameter("ktoken", jobInfo.getKtoken())
                .addQueryParameter("cpi", jobInfo.getCpi())
                .addQueryParameter("ut", "s")
                .addQueryParameter("clazzId", course.getClazzId())
                .addQueryParameter("enc", job.getEnc())
                .addQueryParameter("mooc2", "1")
                .addQueryParameter("courseid", course.getCourseId());

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36 Edg/129.0.0.0")
                .get()
                .build();

        Response response = client.newCall(request).execute();
        originHtmlContent = response.body().string();

        // 解析 HTML 获取题目信息
        List<Question> questions = parseQuestions(originHtmlContent);

//        System.out.println(Decode.decodeQuestionsInfo(originHtmlContent));

//        // 发送题目列表给 AI 获取答案
        sendQuestionsToAI(questions);
//
//
//        // 提交作业
        submitAnswers(questions, client);
    }

    /**
     * 解析题目信息
     *
     * @param html 原始html
     */
    public static List<Question> parseQuestions(String html) {


        List<Question> questions = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        // 选择所有题目容器
        Elements questionElements = doc.select("div.TiMu.newTiMu");

        for (Element questionElem : questionElements) {
            // 获取题目类型
            Element typeElem = questionElem.selectFirst("span.newZy_TItle");
            String type = (typeElem != null) ? typeElem.text().replaceAll("[【】]", "").trim() : "未知题型";

            // 获取题干
            Element titleElem = questionElem.selectFirst("div.font-cxsecret.fontLabel");
            String fullTitle = (titleElem != null) ? titleElem.text().trim() : "未知题目";
            String title = fullTitle.replace(typeElem.text(), "").trim(); // 去掉题型部分

            List<String> options = new ArrayList<>();
            Elements optionElements;

            // 根据题型选择不同的解析方式
            switch (type) {
                case "单选题":
                    optionElements = questionElem.select("li.before-after[role=radio]"); // 单选题
                    break;
                case "判断题":
                    optionElements = questionElem.select("li.before-after[role=radio]"); // 判断题（可能与单选题相同）
                    break;
                case "多选题":
                    optionElements = questionElem.select("li.before-after-checkbox"); // 多选题
                    break;
                default:
                    optionElements = questionElem.select("li"); // 其他类型默认解析
                    break;
            }

            for (Element optionElem : optionElements) {
                String optionLetter = optionElem.selectFirst("span.num_option") != null
                        ? optionElem.selectFirst("span.num_option").text().trim()
                        : "";

                String optionText = optionElem.selectFirst("a") != null
                        ? optionElem.selectFirst("a").text().trim()
                        : "";

                options.add(optionLetter + ": " + optionText);
            }

            questions.add(new Question(title, String.join("; ", options), type));
        }

        return questions;
    }


    private static List<String> multiCut(String options) {
        String[] delimiters = {",", "，", "|", "\n", "\r", "\t", "#", "*", "-", "_", "+", "@", "~", "/", "\\", ".", "&", " "};
        for (String delimiter : delimiters) {
            if (options.contains(delimiter)) {
                return Arrays.asList(options.split(Pattern.quote(delimiter)));
            }
        }
        return Arrays.asList("A", "B", "C", "D");
    }

    // 将题目列表转换为 JSON 并发送到 AI 服务器
    private static void sendQuestionsToAI(List<Question> questions) {
        GenerationResult generationResult;
        try {
            ObjectMapper objectMapper = new ObjectMapper();


            generationResult = QWen.staticCallWithMessage(objectMapper.writeValueAsString(questions));


            Map<String, String> answerMap = parseAIResponse(generationResult.getOutput().getChoices().get(0).getMessage().getContent());
            System.out.println("AI 响应: " + generationResult.getOutput().getChoices().get(0).getMessage().getContent());
            System.out.println("AI 响应解析结果: " + answerMap);
        } catch (Exception e) {

            System.err.println("错误信息：" + e.getMessage());
            System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");

            System.err.println("发送 AI 请求失败: " + e.getMessage());

        }
    }

    // 解析 AI 返回的答案
    private static Map<String, String> parseAIResponse(String jsonResponse) {
        Map<String, String> answerMap = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            answerMap = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            System.err.println("解析 AI 返回数据失败: " + e.getMessage());
        }
        return answerMap;
    }

    private void submitAnswers(List<Question> questions, OkHttpClient client) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Question q : questions) {
            formBuilder.add("answer" + q.getId(), q.getAnswer());
        }

        Request request = new Request.Builder()
                .url("https://mooc1.chaoxing.com/mooc-ans/work/addStudentWorkNew")
                .post(formBuilder.build())
                .header("X-Requested-With", "XMLHttpRequest")
                .build();

        Response response = client.newCall(request).execute();
        System.out.println("提交结果: " + response.body().string());
    }


}

