package com.tihai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tihai.domain.chaoxing.WkUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通-用户mapper
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
@Mapper
public interface SuperStarUserMapper extends BaseMapper<WkUser> {
}

