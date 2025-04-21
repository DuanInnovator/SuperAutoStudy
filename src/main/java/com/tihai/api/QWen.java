package com.tihai.api;

import com.alibaba.dashscope.aigc.generation.GenerationResult;

import java.util.Arrays;
import java.lang.System;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.tihai.properties.LargeModelProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Copyright : DuanInnovator
 * @Description : QWen接口
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/3
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
public class QWen {

    private static QWen instance;
    @Autowired
    private LargeModelProperties largeModelProperties;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public GenerationResult callWithMessage(String content) throws ApiException, NoApiKeyException, InputRequiredException {

        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful assistant.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("你帮我返回问题的答案，使用json格式返回,并且只需返回json数据，其他不用任务信息，谢谢你"+"题目列表:"+content)
                .build();

        System.out.println(userMsg.getContent());
        GenerationParam param = GenerationParam.builder()
                .apiKey(largeModelProperties.getQWenApiKey())
                // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        return gen.call(param);
    }

    public static GenerationResult staticCallWithMessage(String content) throws ApiException, NoApiKeyException, InputRequiredException {
        if (instance == null) {
            throw new IllegalStateException("QWen 实例尚未初始化");
        }
        return instance.callWithMessage(content);
    }

}

