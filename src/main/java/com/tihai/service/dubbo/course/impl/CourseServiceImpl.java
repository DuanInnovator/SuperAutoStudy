package com.tihai.service.dubbo.course.impl;

import com.tihai.dubbo.pojo.course.Course;
import com.tihai.domain.chaoxing.WkUser;
import com.tihai.dubbo.dto.WKUserDTO;
import com.tihai.enums.BizCodeEnum;
import com.tihai.service.dubbo.course.CourseService;
import com.tihai.service.superstar.SuperStarLoginService;
import com.tihai.utils.CourseUtil;
import ma.glasnost.orika.MapperFacade;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description : Dubbo-提供课程查询接口实现
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/17
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@DubboService
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private SuperStarLoginService loginService;
    @Autowired
    private CourseUtil courseUtil;

    @Autowired
    private MapperFacade mapperFacade;

    /**
     * 获取课程列表
     * @param dto 用户登录信息
     */
    @Override
    public List<Course> getCourseList(WKUserDTO dto,boolean isUpdate) throws IOException {
        String cookies = loginService.login(mapperFacade.map(dto, WkUser.class), isUpdate);
        if(cookies!=null){
            courseUtil.setCookies(cookies);
            List<Course> courseList = courseUtil.getCourseList();
            return courseList;

        }else {
            throw new RuntimeException(BizCodeEnum.ACCOUNT_OR_PASSWORD_ERROR.getMsg());
        }

    }
}

