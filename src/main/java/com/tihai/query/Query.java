package com.tihai.query;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Copyright : DuanInnovator
 * @Description : 查询分页
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/10
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
@Accessors(chain = true)
public class Query {


    /**
     * 分页
     */
    private Integer page=0;

    /**
     * 分页大小
     */
    private Integer pageSize=10;


}
