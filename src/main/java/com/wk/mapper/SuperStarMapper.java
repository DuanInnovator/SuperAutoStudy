package com.wk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wk.domain.chaoxing.SuperStarTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Copyright : DuanInnovator
 * @Description : 超星学习通mapper
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
@Mapper
public interface SuperStarMapper extends BaseMapper<SuperStarTask> {
}

