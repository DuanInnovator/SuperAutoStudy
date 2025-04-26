package com.tihai.common;

import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private String id;
    private String title;
    private String options;

    private List<String> tKOptions = new ArrayList<>();
    private Integer type;
    private String answer;
    private AnswerField<String> answerField;

    public Question(String qData, String title, String options, Integer type, AnswerField<String> answerField) {
        this.answerField = answerField;
        this.id = qData;
        this.title = title;
        this.options = options;
        this.type = type;
        this.answer = "";
    }

    /**
     * 更新answerField中的值
     */
    public void updateAnswerField() {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put(this.answerField.getAnswerKey(), this.answer);
        fieldMap.put(this.answerField.getAnswerTypeKey(), this.answerField.getAnswerTypeValue());
        this.answerField.updateFromMap(fieldMap);
    }


}

