package com.tihai.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description : WebClient
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/24
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Configuration
public class WebClientConfig {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public ConnectionProvider connectionProvider() {
        // 创建带最大连接数的连接池
        return ConnectionProvider.builder("tikuConnectionPool")
                .maxConnections(200)      // 最大连接数
                .pendingAcquireTimeout(Duration.ofSeconds(10)) // 请求超时
                .build();
    }

    @Bean
    public WebClient tikuWebClient(ConnectionProvider connectionProvider, TiKuConfig tiKuConfig) {
        return WebClient.builder()
                .baseUrl(tiKuConfig.getEndpoints().get(0).getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create(connectionProvider)
                                .responseTimeout(Duration.ofSeconds(60))
                ))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(addTokenToBodyFilter(tiKuConfig.getEndpoints().get(0).getToken()))
//                .filter(logResponse())
                .build();
    }

//    /**
//     * 响应日志
//     * @return ExchangeFilterFunction 拦截器
//     */
//    private ExchangeFilterFunction logResponse() {
//        return ExchangeFilterFunction.ofResponseProcessor(response -> {
//            System.out.println("Response status: " +response.bodyToMono(TiKuResponse.class).toString());
//            return Mono.just(response);
//        });
//    }


    /**
     * 构建一个过滤器，把 token 插入请求体
     */
    public static ExchangeFilterFunction addTokenToBodyFilter(String token) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // 只对 POST/PUT/PATCH 这种有 body 的请求生效
            if (HttpMethod.GET.equals(clientRequest.method()) || HttpMethod.DELETE.equals(clientRequest.method())) {
                return Mono.just(clientRequest);
            }

            return DataBufferUtils.join((Publisher<? extends DataBuffer>) clientRequest.body())
                    .flatMap(dataBuffer -> {
                        try {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);

                            String bodyStr = new String(bytes);
                            Object newBody;

                            if (bodyStr.isEmpty()) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("token", token);
                                newBody = map;
                            } else if (bodyStr.trim().startsWith("{")) {
                                Map<String, Object> map = objectMapper.readValue(bodyStr, Map.class);
                                map.put("token", token);
                                newBody = map;
                            } else {
                                Map<String, Object> map = new HashMap<>();
                                map.put("data", bodyStr);
                                map.put("token", token);
                                newBody = map;
                            }

                            BodyInserter<Object, ReactiveHttpOutputMessage> inserter =
                                    BodyInserters.fromValue(newBody);

                            ClientRequest newRequest = ClientRequest.from(clientRequest)
                                    .body(inserter)
                                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .build();

                            return Mono.just(newRequest);

                        } catch (JsonProcessingException e) {
                            return Mono.error(e);
                        }
                    });
        });
    }

}

