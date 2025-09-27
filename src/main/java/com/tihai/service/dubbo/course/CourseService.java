package com.tihai.service.dubbo.course;

import com.tihai.dubbo.pojo.course.Course;
import com.tihai.dubbo.dto.WKUserDTO;

import java.io.IOException;
import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description :Dubbo-提供课程查询接口
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/17
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
public interface CourseService {

    /**
     * 获取课程列表
     * @param dto 用户登录信息
     */
    List<Course> getCourseList(WKUserDTO dto) throws IOException;
}

