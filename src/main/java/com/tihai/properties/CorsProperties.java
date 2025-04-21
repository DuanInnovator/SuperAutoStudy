package com.tihai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Copyright : DuanInnovator
 * @Description : 跨域允许地址配置
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/5
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Configuration
@ConfigurationProperties(prefix = "cors.allow")
@Data
public class CorsProperties {
    /**
     * 允许跨域的域名
     */
    private String[] origins;
}

