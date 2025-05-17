package com.tihai.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description :  答案字段
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/24
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@Data
@NoArgsConstructor
public class AnswerField<T> {
    private String answerKey;
    private T answerValue;
    private String answerTypeKey;
    private String answerTypeValue;

    public AnswerField(String qData, String qTypeCode, T defaultValue) {
        this.answerKey = "answer" + qData;
        this.answerValue = defaultValue;
        this.answerTypeKey = "answertype" + qData;
        this.answerTypeValue = qTypeCode;
    }



    public void updateFromMap(Map<String, Object> fieldMap) {
        this.answerValue = (T) fieldMap.get(this.answerKey);
        this.answerTypeValue = (String) fieldMap.get(this.answerTypeKey);
    }
}
