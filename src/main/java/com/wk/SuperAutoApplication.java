package com.wk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @Copyright : DuanInnovator
 * @Description :启动类
 * @Author : DuanInnovator
 * @CreateTime : 2025/2/28
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SpringBootApplication()
@EnableConfigurationProperties
public class SuperAutoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuperAutoApplication.class, args);

    }
}

