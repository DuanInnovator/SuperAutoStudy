package com.tihai;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Copyright : DuanInnovator
 * @Description :启动类
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/28
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SpringBootApplication()
@EnableConfigurationProperties
@EnableDubbo
@EnableDiscoveryClient
@MapperScan("com.tihai.mapper")
@EnableScheduling
public class SuperAutoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuperAutoApplication.class, args);

    }
}

