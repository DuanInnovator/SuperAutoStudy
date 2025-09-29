package com.tihai.service.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Copyright : DuanInnovator
 * @Description : nacos 实例服务
 * @Author : DuanInnovator
 * @CreateTime : 2025/3/25
 * @Link : <a href="https://github.com/DuanInnovator/SuperAutotudy">...</a>
 **/
@SuppressWarnings("all")
@Service
@ConditionalOnProperty(name = "enable-service-registry", havingValue = "true")
public class NacosInstanceService {

    @Autowired
    private NacosServiceManager nacosServiceManager;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    public String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress(); // 获取当前机器的 IP 地址
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to get local IP address", e);
        }
    }

    public int getServerPort() {
        return nacosDiscoveryProperties.getPort(); // 获取当前服务的端口
    }

    /**
     * 获取当前服务的实例ID
     * @return
     * @throws NacosException
     */
    public String getInstanceId() throws NacosException {
        return String.format("%s:%d", getServerIp(), getServerPort());
    }
}

