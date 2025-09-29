package com.tihai.common;

import lombok.Data;

import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description : 课程点
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
public class CoursePoint {
    private boolean hasLocked;
    private List<ChapterPoint> points;
}

