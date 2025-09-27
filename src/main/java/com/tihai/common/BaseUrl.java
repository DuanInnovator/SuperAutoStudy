package com.tihai.common;

/**
 * @Copyright : DuanInnovator
 * @Description : 基本URL
 * @Author : DuanInnovator
 * @CreateTime : 2025/9/21
 * @Link : <a href="https://github.com/DuanInnovator/SuperTiKu">...</a>
 **/
public class BaseUrl {

    public static final String API_SSO_LOGIN = "https://sso.chaoxing.com/apis/login/userLogin4Uname.do";
    public static final String LOGIN_URL = "https://passport2.chaoxing.com/fanyalogin";
    public static final String VIDEO_STATUS_URL = "https://mooc1.chaoxing.com/ananas/status/%s?k=%s&flag=normal";


    public static final String VIDEO_PROCESS_LOG_BASE_URL = "https://mooc1.chaoxing.com/mooc-ans/multimedia/log/a/";
    public static final String VIDEO_PROCESS_LOG_URL = "https://mooc1.chaoxing.com/mooc-ans/multimedia/log/a/%s/%s?clazzId=%s&playingTime=%s&duration=%s&clipTime=0_%s&objectId=%s&%sjobid=%s&userid=%s&isdrag=3&view=pc&enc=%s&rt=%s&dtype=%s&_t=%s";

    public static final String COURSE_LIST_URL = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/courselistdata";

    public static final String COURSE_FILE_URL = "https://mooc2-ans.chaoxing.com/mooc2-ans/visit/interaction";


    public static final String COURSE_POINT_URL = "https://mooc2-ans.chaoxing.com/mooc2-ans/mycourse/studentcourse?courseid=%s&clazzid=%s&cpi=%s&ut=s";

    public static final String JOB_LIST_URL = "https://mooc1.chaoxing.com/mooc-ans/knowledge/cards?clazzid=%s&courseid=%s&knowledgeid=%s&num=%s&ut=s&cpi=%s&v=20160407-3&mooc2=1";

    public static final String DOCUMENT_URL = "https://mooc1.chaoxing.com/ananas/job/document?jobid=%s&knowledgeid=%s&courseid=%s&clazzid=%s&jtoken=%s&_dc=%s";

    public static final String READ_URL = "https://mooc1.chaoxing.com/ananas/job/readv2";

    public static final String WORK_URL = "https://mooc1.chaoxing.com/mooc-ans/api/work";

    public static final String SCORE_URL = "https://stat2-ans.chaoxing.com/study-data/index?courseid=%s&clazzid=%s&cpi=%s&ut=s";

    public static final String SUBMIT_ANSWER_URL = "https://mooc1.chaoxing.com/mooc-ans/work/addStudentWorkNew";
}

