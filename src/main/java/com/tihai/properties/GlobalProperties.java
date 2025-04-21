package com.tihai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * @Copyright : DuanInnovator
 * @Description : 全局配置信息
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/1
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
@ConfigurationProperties(prefix = "super-star") //扫描配置文件,自动获取值
@Data
@SuppressWarnings("all")
public class GlobalProperties {
    // AES 加密密钥
    public String aesKey;

    // 操作间隔阈值（秒）
    public int threShold;

    // 通用请求头
    public Map<String, String> headers;
    // 视频相关请求头
    public Map<String, String> videoHeaders;

    // 音频相关请求头
    public Map<String, String> audioHeaders;


}

