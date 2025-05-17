package com.tihai.utils;


import com.tihai.common.*;
import com.tihai.dubbo.pojo.course.Course;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Copyright : DuanInnovator
 * @Description : 课程解码
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
public class Decode {
    /**
     * 解码课程列表
     */
    public static List<Course> decodeCourseList(String html) {
//        LoggerUtil.logger.trace("开始解码课程列表...");
        List<Course> courseList = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements rawCourses = doc.select("div.course");
        Pattern cpiPattern = Pattern.compile("cpi=(.*?)&");

        for (Element course : rawCourses) {
            // 排除未开放的课程：含有 a.not-open-tip 或 div.not-open-tip 的忽略
            if (!course.select("a.not-open-tip").isEmpty() || !course.select("div.not-open-tip").isEmpty()) {
                continue;
            }
            Course courseDetail = new Course();
            courseDetail.setId(course.attr("id"));
            courseDetail.setInfo(course.attr("info"));
            courseDetail.setRoleId(course.attr("roleid"));
            courseDetail.setClazzId(course.select("input.clazzId").attr("value"));
            courseDetail.setCourseId(course.select("input.courseId").attr("value"));

            // 从 a 标签的 href 中提取 cpi
            String href = course.select("a").attr("href");
            Matcher m = cpiPattern.matcher(href);
            if (m.find()) {
                courseDetail.setCpi(m.group(1));
            } else {
                courseDetail.setCpi("");
            }
            courseDetail.setTitle(course.select("span.course-name").attr("title"));
            Element descEl = course.select("p.margint10").first();
            courseDetail.setDesc(descEl != null ? descEl.attr("title") : "");
            courseDetail.setTeacher(course.select("p.color3").attr("title"));
            courseList.add(courseDetail);
        }
        return courseList;
    }

    /**
     * 解码二级课程列表（文件夹）
     */
    public static List<CourseFolder> decodeCourseFolder(String html) {
//        LoggerUtil.logger.trace("开始解码二级课程列表...");
        List<CourseFolder> folderList = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements rawFolders = doc.select("ul.file-list > li");
        for (Element folder : rawFolders) {
            String fileId = folder.attr("fileid");
            if (fileId != null && !fileId.isEmpty()) {
                CourseFolder courseFolder = new CourseFolder();
                courseFolder.setId(fileId);
                courseFolder.setRename(folder.select("input.rename-input").attr("value"));
                folderList.add(courseFolder);
            }
        }
        return folderList;
    }

    /**
     * 解码章节列表（课程点）
     */
    public static CoursePoint decodeCoursePoint(String html) {
//        LoggerUtil.logger.trace("开始解码章节列表...");
        CoursePoint coursePoint = new CoursePoint();
        coursePoint.setHasLocked(false);
        List<ChapterPoint> points = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements chapterUnits = doc.select("div.chapter_unit");
        Pattern idPattern = Pattern.compile("^cur(\\d{1,20})$");

        for (Element chapter : chapterUnits) {
            Elements rawPoints = chapter.select("li");
            for (Element li : rawPoints) {
                Element pointDiv = li.select("div").first();
                if (pointDiv == null || !pointDiv.hasAttr("id")) {
                    continue;
                }
                Matcher m = idPattern.matcher(pointDiv.attr("id"));
                if (!m.find()) {
                    continue;
                }
                ChapterPoint pointDetail = new ChapterPoint();
                pointDetail.setId(m.group(1));
                String title = pointDiv.select("a.clicktitle").text().replace("\n", "").trim();
                pointDetail.setTitle(title);
                // 默认为1
                pointDetail.setJobCount(1);
                Element knowledgeJobCount = pointDiv.select("input.knowledgeJobCount").first();
                if (knowledgeJobCount != null) {
                    pointDetail.setJobCount(Integer.parseInt(knowledgeJobCount.attr("value")));
                } else {
                    Element tipEl = pointDiv.select("span.bntHoverTips").first();
                    if (tipEl != null && tipEl.text().contains("解锁")) {
                        coursePoint.setHasLocked(true);
                    }
                }
                points.add(pointDetail);
            }
        }
        coursePoint.setPoints(points);
        return coursePoint;
    }

    /**
     * 解码任务点列表
     * 返回 Pair(任务点列表, 任务附加信息)
     */
    public static Pair<List<Job>, JobInfo> decodeCourseCard(String html) {
//        LoggerUtil.logger.trace("开始解码任务点列表...");
        JobInfo jobInfo = new JobInfo();
        List<Job> jobList = new ArrayList<>();

        if (html.contains("章节未开放")) {
            jobInfo.setNotOpen(true);
            return new Pair<>(jobList, jobInfo);
        }

        // 去除空格后匹配 mArg={...};
        String cleaned = html.replace(" ", "");
        Pattern pattern = Pattern.compile("mArg=\\{(.*?)\\};");
        Matcher matcher = pattern.matcher(cleaned);
        String temp = "";
        if (matcher.find()) {
            temp = matcher.group(1);
        } else {
            return new Pair<>(jobList, new JobInfo());
        }
        // 构造 JSON 字符串，并解析
        JSONObject jsonObj = new JSONObject("{" + temp + "}");
        if (jsonObj != null) {
            JSONObject defaults = jsonObj.getJSONObject("defaults");
            jobInfo.setKtoken(defaults.optString("ktoken"));
            jobInfo.setMtEnc(defaults.optString("mtEnc"));
            jobInfo.setReportTimeInterval(defaults.optInt("reportTimeInterval"));
            jobInfo.setDefenc(defaults.optString("defenc"));
            jobInfo.setCardid(defaults.optString("cardid"));
            jobInfo.setCpi(defaults.optString("cpi"));
            jobInfo.setQnenc(defaults.optString("qnenc"));
            jobInfo.setKnowledgeid(defaults.optString("knowledgeid"));

            JSONArray attachments = jsonObj.getJSONArray("attachments");
            for (int i = 0; i < attachments.length(); i++) {
                JSONObject card = attachments.getJSONObject(i);
                // 已通过的任务
                if (card.optBoolean("isPassed", false)) {
                    continue;
                }
                // 不属于任务点的任务
                if (!card.has("job") || !card.getBoolean("job")) {
                    if (card.has("type") && "read".equals(card.getString("type"))) {
                        JSONObject property = card.optJSONObject("property");
                        if (property != null && property.optBoolean("read", false)) {
                            // 已阅读，跳过
                            continue;
                        }
                        Job job = new Job();
                        assert property != null;
                        job.setTitle(property.optString("title"));
                        job.setType("read");
                        job.setId(property.optString("id"));
                        job.setJobId(card.optString("jobid"));
                        job.setJToken(card.optString("jtoken"));
                        job.setMid(card.optString("mid"));
                        job.setOtherInfo(card.optString("otherInfo"));
                        job.setEnc(card.optString("enc"));
                        job.setAid(card.optString("aid"));
                        jobList.add(job);
                    }
                    continue;
                }
                // 视频任务
                if ("video".equals(card.optString("type"))) {
                    Job job = new Job();
                    job.setType("video");
                    job.setJobId(card.optString("jobid"));
                    JSONObject property = card.optJSONObject("property");
                    if (property != null) {
                        job.setName(property.optString("name"));
                    }
                    job.setOtherInfo(card.optString("otherInfo"));
                    if (!card.has("mid")) {
//                        LoggerUtil.logger.error("出现转码失败视频，已跳过...");
                        continue;
                    }
                    job.setMid(card.optString("mid"));
                    job.setObjectId(card.optString("objectId"));
                    job.setAid(card.optString("aid"));
                    jobList.add(job);
                    continue;
                }
                // 文档任务
                if ("document".equals(card.optString("type"))) {
                    Job job = new Job();
                    job.setType("document");
                    job.setJobId(card.optString("jobid"));
                    job.setOtherInfo(card.optString("otherInfo"));
                    job.setJToken(card.optString("jtoken"));
                    job.setMid(card.optString("mid"));
                    job.setEnc(card.optString("enc"));
                    job.setAid(card.optString("aid"));
                    JSONObject property = card.optJSONObject("property");
                    if (property != null) {
                        job.setObjectId(property.optString("objectid"));
                    }
                    jobList.add(job);
                    continue;
                }
                // 作业任务（workid 类型）
                if ("workid".equals(card.optString("type"))) {
                    Job job = new Job();
                    job.setType("workid");
                    job.setJobId(card.optString("jobid"));
                    job.setOtherInfo(card.optString("otherInfo"));
                    job.setMid(card.optString("mid"));
                    job.setEnc(card.optString("enc"));
                    job.setAid(card.optString("aid"));

                    jobList.add(job);
                    continue;
                }
                // 调查问卷（vote）等任务忽略
                if ("vote".equals(card.optString("type"))) {
                    continue;
                }
            }
        }
        return new Pair<>(jobList, jobInfo);
    }

    /**
     * 解码题目和答案表单信息
     */
    //TODO 待优化
    public static Map<String, Object> decodeQuestionsInfo(String htmlContent) throws Exception {
        Document doc = Jsoup.parse(htmlContent);
        Map<String, Object> formData = new HashMap<>();
        Element formTag = doc.selectFirst("form");
        if (formTag == null) {
            return formData;
        }
        // 初始化字体解码器
        FontDecoder fd = new FontDecoder(htmlContent);

        // 提取表单中非答题 input 元素
        Elements inputs = formTag.select("input");
        for (Element input : inputs) {
            if (!input.hasAttr("name") || input.attr("name").contains("answer")) {
                continue;
            }
            formData.put(input.attr("name"), input.attr("value"));
        }

        // 初始化题目列表
        List<Map<String, Object>> questions = new ArrayList<>();
        Elements quesDivs = formTag.select("div.singleQuesId");
        for (Element divTag : quesDivs) {
            String qData = divTag.attr("data");
            // 题目标题（经过字体解码与换行替换）
            String rawTitle = divTag.selectFirst("div.Zy_TItle").text();
            String qTitle = replaceRtn(fd.decode(rawTitle));
            // 题目选项
            StringBuilder qOptionsSb = new StringBuilder();
            Elements liTags = divTag.select("ul li");
            for (Element li : liTags) {
                qOptionsSb.append(replaceRtn(fd.decode(li.text()))).append("\n");
            }
            String qOptions = qOptionsSb.toString().trim(); // 去除尾部换行

            // 根据 TiMu 中 data 属性判断题型
            Element tiMu = divTag.selectFirst("div.TiMu");
            String qTypeCode = tiMu.attr("data");
            String qType;
            switch (qTypeCode) {
                case "0":
                    qType = "single";
                    break;
                case "1":
                    qType = "multiple";
                    break;
                case "2":
                    qType = "completion";
                    break;
                case "3":
                    qType = "judgement";
                    break;
                default:
                    qType = "unknown";
                    break;
            }
            Map<String, Object> question = new HashMap<>();
            question.put("id", qData);
            question.put("title", qTitle);
            question.put("options", qOptions);
            question.put("type", qType);
            Map<String, Object> answerField = new HashMap<>();
            answerField.put("answer" + qData, "");
            answerField.put("answertype" + qData, qTypeCode);
            question.put("answerField", answerField);
            questions.add(question);
        }
        formData.put("questions", questions);

        // 拼接 answerwqbid 字段：所有题目 id 用逗号隔开，末尾多一个逗号
        StringBuilder answerwqbidSb = new StringBuilder();
        for (Map<String, Object> q : questions) {
            answerwqbidSb.append(q.get("id")).append(",");
        }
        formData.put("answerwqbid", answerwqbidSb.toString());
        return formData;

    }

    /**
     * 辅助方法：替换字符串中的 \r、\t、\n
     */
    private static String replaceRtn(String text) {
        if (text == null) return "";
        return text.replace("\r", "").replace("\t", "").replace("\n", "");
    }
}

