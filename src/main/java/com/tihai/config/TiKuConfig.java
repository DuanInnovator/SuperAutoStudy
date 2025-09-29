package com.tihai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


/**
 * @Copyright : DuanInnovator
 * @Description : 题库配置类
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/24
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "tiku.settings")
public class TiKuConfig {

    private List<Endpoint> endpoints=new ArrayList<>();

    @Data
    public static class Endpoint {

        /**
         * 题库地址
         **/
        private String baseUrl;

        /**
         * token
         **/
        private String token;

        /**
         * 是否启用模型搜索
         */
        private Boolean isEnableSearch;
    }


}