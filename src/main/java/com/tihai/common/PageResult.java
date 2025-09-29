package com.tihai.common;

import lombok.Data;

import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description : 分页结果
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/22
 * @Link :<a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
public class PageResult<T> {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<T> data;
}