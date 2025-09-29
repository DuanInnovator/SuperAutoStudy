package com.tihai.utils;

import com.alibaba.nacos.api.exception.NacosException;
import com.tihai.service.nacos.NacosInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * @Copyright : DuanInnovator
 * @Description : 获取服务信息
 * @Author : DuanInnovator
 * @CreateTime : 2025/4/26
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@Component
@SuppressWarnings("all")
public class ServerInfoUtil {

    @Autowired(required = false)
    private NacosInstanceService nacosInstanceService;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 获取当前服务信息
     * @return ip:port
     * @throws NacosException nacos异常
     */

    public String getCurrentServerInstance() throws NacosException {
        return (nacosInstanceService != null)
                ? nacosInstanceService.getInstanceId()
                : this.getServerAddress();
    }

    /**
     * 获取服务器IP
     */
    private String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /**
     * 获取服务器端口
     */
    private int getServerPort() {
        try {
            return ((ServletWebServerApplicationContext) applicationContext)
                    .getWebServer().getPort();
        } catch (Exception e) {
            return 3030;
        }
    }

    /**
     * 获取完整地址 (http://ip:port)
     */
    private String getServerAddress() {
        return String.format("%s:%d", getServerIp(), getServerPort());
    }
}