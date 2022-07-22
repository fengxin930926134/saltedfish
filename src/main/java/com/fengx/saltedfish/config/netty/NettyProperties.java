package com.fengx.saltedfish.config.netty;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "netty")
@Data
@Configuration
public class NettyProperties {
    /**
     * boss线程数量 默认为cpu线程数*2
     */
    private Integer boss = 2;
    /**
     * worker线程数量 默认为cpu线程数*2
     */
    private Integer worker = 2;
    /**
     * 连接超时时间 默认为30s
     */
    private Integer timeout = 30000;
    /**
     * 读取消息超时断开连接时间 默认一分钟
     */
    private Integer readTimout = 60;
    /**
     * 服务器主端口 默认7000
     */
    private Integer port = 7000;
    /**
     * 服务器备用端口 默认70001
     */
    private Integer portSalve = 7001;
    /**
     * 服务器地址 默认为本地
     */
    private String host = "127.0.0.1";
}