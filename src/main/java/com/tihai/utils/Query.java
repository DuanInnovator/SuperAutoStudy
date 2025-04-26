package com.tihai.utils;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tihai.common.Question;
import com.tihai.config.TiKuConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Copyright : DuanInnovator
 * @Description : 查题工具类
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/24
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@Component
@Slf4j
public class Query {

    @Autowired
    private WebClient webClient;

    @Autowired
    private TiKuConfig tiKuConfig;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 查询题目返回答案
     * @param question 题目信息
     * @return 答案
     */
//    public Mono<String> queryAnswer(Question question) {
//        String queryParams = String.format("use=%s&%sToken=%s",
//                tiKuConfig.getName(),
//                tiKuConfig.getName(),
//                tiKuConfig.getToken());
//
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("question", question.getTitle());
//        requestBody.put("options", question.getOptions());
//        requestBody.put("type", question.getType());
//
//        return webClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/")
//                        .query(queryParams)
//                        .build())
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .retrieve()
//                .onStatus(HttpStatus::isError, response ->
//                        response.bodyToMono(String.class)
//                                .doOnNext(errorBody -> log.error("题库请求失败，状态码: {}, 响应体: {}", response.statusCode(), errorBody))
//                                .flatMap(errorBody -> Mono.error(new RuntimeException("题库请求失败: " + response.statusCode())))
//                )
//                .bodyToMono(TiKuResponse.class)
//                .doOnNext(response -> {
//                    try {
//                        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response);
//                        log.error("题库返回的答案: \n{}", json);
//                    } catch (Exception e) {
//                        log.error("无法将响应转换为 JSON 字符串", e);
//                    }
//                })
//                .doOnError(error -> log.error("题库请求异常: {}", error.getMessage()))
//                .flatMap(response -> {
//                    if (response.getAnswer() != null &&
//                            response.getAnswer().getAnswerKey() != null &&
//                            !response.getAnswer().getAnswerKey().isEmpty()) {
//
//                        String answer = String.join(",", response.getAnswer().getAnswerKey());
//                        return Mono.just(answer);
//                    }
//                    return Mono.error(new RuntimeException("题库返回的答案为空"));
//                });
//    }

    /**
     * 同步查询题目返回答案
     *
     * @param question 题目信息
     * @return 答案
     * @throws IOException
     */
    public String queryAnswerSync(Question question) throws IOException {


        // 构造请求体（JSON）
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("question", question.getTitle());
        requestBody.put("options", question.getTKOptions());
        requestBody.put("type", question.getType());
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .build();

        HttpUrl url = buildMultiSearchUrl(tiKuConfig.getEndpoints());
        // 构造 Request
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(
                        requestBodyJson,
                        MediaType.parse("application/json; charset=utf-8")  // 明确指定 JSON 编码
                ))
                .build();
        // 发送同步请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("题库请求失败" + response.message());
                return null;
            }

            // 获取响应体（JSON 字符串）
            String responseBody = response.body().string();

            // 解析 JSON
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode answerNode = rootNode.path("answer");

            if (answerNode.isMissingNode() || answerNode.path("answerKey").isEmpty()) {
                return null;
            }

            // 提取 answerKey
            return answerNode.path("answerKey").toString();
        }
    }

    /**
     * 构建多题库搜索URL
     *
     * @param endpoints 题库配置
     * @return 构建后的URL
     */
    public HttpUrl buildMultiSearchUrl(List<TiKuConfig.Endpoint> endpoints) {

        StringBuilder queryString = new StringBuilder();

        String useValue = endpoints.stream()
                .map(TiKuConfig.Endpoint::getName)
                .collect(Collectors.joining(","));
        queryString.append("use=").append(useValue);

        endpoints.forEach(endpoint -> {
            if (StrUtil.isNotBlank(endpoint.getToken())) {
                queryString.append("&")
                        .append(endpoint.getName())
                        .append("Token=")
                        .append(endpoint.getToken());
            }
        });

        String rawUrl = tiKuConfig.getBaseUrl() + "?" + queryString;
        return HttpUrl.parse(rawUrl);
    }

}

