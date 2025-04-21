package com.tihai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Copyright : DuanInnovator
 * @Description : 学习配置
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/15
 * @Link : <a href="https://github.com/DuanInnovator/TiHaiWuYou-Admin/tree/mine-admin">...</a>
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "study.chaoxing")
public class StudyProperties {
    /** 学习速度 **/
    private Integer speed;
}

