package com.wk.common;

import lombok.Data;


/**
 * @Copyright : DuanInnovator
 * @Description : 题目实体
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
public class Question {
    private static int count = 0;
    private final int id;
    private final String title;
    private final String options;
    private final String type;
    private String answer;

    public Question(String title, String options, String type) {
        this.id = ++count;
        this.title = title;
        this.options = options;
        this.type = type;
        this.answer = "";
    }
}

