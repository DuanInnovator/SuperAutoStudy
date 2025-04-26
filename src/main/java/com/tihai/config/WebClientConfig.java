package com.tihai.config;

import com.tihai.common.TiKuResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * @Copyright : DuanInnovator
 * @Description : WebClient
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/24
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@Configuration
public class WebClientConfig {

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
                .baseUrl(tiKuConfig.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create(connectionProvider)
                                .responseTimeout(Duration.ofSeconds(60))
                ))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logResponse())
                .build();
    }

    /**
     * 响应日志
     * @return ExchangeFilterFunction 拦截器
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            System.out.println("Response status: " +response.bodyToMono(TiKuResponse.class).toString());
            return Mono.just(response);
        });
    }


}