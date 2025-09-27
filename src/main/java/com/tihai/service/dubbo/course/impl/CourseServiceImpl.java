package com.tihai.service.dubbo.course.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tihai.config.OkHttpClientInit;
import com.tihai.dubbo.pojo.course.Course;
import com.tihai.domain.chaoxing.WkUser;
import com.tihai.dubbo.dto.WKUserDTO;
import com.tihai.enums.BizCodeEnum;
import com.tihai.service.dubbo.course.CourseService;
import com.tihai.service.superstar.SuperStarLoginService;
import com.tihai.service.superstar.SuperStarUserService;
import com.tihai.utils.CourseUtil;
import ma.glasnost.orika.MapperFacade;
import okhttp3.CookieJar;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description : Dubbo-提供课程查询接口实现
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/17
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@DubboService
@Service
@SuppressWarnings("all")
public class CourseServiceImpl implements CourseService {

    @Autowired
    private SuperStarLoginService loginService;

    @Autowired
    private SuperStarUserService superStarUserService;
    @Autowired
    private CourseUtil courseUtil;

    @Autowired
    private MapperFacade mapperFacade;

    /**
     * 获取课程列表
     *
     * @param userDTO 用户登录信息
     */
    @Override
    public List<Course> getCourseList(WKUserDTO userDTO) throws IOException {

        CookieJar cookieJar = loginService.login(mapperFacade.map(userDTO, WkUser.class));

        if (cookieJar != null) {

            Map<String, Object> loginInfo = loginService.getLoginInfo(
                    OkHttpClientInit.getClient(userDTO.getAccount(), false, false)
            );
            if (loginInfo != null) {
                WkUser userByAccount = superStarUserService.getUserByAccount(userDTO.getAccount());
                if (userByAccount == null) {
                    WkUser wkUser = mapperFacade.map(userDTO, WkUser.class);
                    wkUser.setId(IdWorker.getId());
                    wkUser.setSchoolName(loginInfo.get("schoolname").toString());
                    wkUser.setName(loginInfo.get("name").toString());
                    superStarUserService.save(wkUser);
                } else if (userByAccount.getSchoolName() == null) {
                    userByAccount.setSchoolName(loginInfo.get("schoolname").toString());
                    userByAccount.setName(loginInfo.get("name").toString());
                    superStarUserService.updateById(userByAccount);
                }
            }
            return courseUtil.getCourseList(userDTO.getAccount());

        } else {
            throw new RuntimeException(BizCodeEnum.ACCOUNT_OR_PASSWORD_ERROR.getMsg());
        }

    }
}

