package com.tihai.common;

import lombok.Data;

import java.util.List;

/**
 * @Copyright : DuanInnovator
 * @Description :题库响应
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/24
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
public class TiKuResponse {
    private Answer answer;

    @Data
    public static class Answer {
        private List<String> answerKey;
    }
}

