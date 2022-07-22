package com.fengx.saltedfish.config.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Slf4j
@RequiredArgsConstructor
public class NettyServerBoot {

    private final ServerBootstrap serverBootstrap;
    private final NioEventLoopGroup boosGroup;
    private final NioEventLoopGroup workerGroup;
    private final NettyProperties nettyProperties;

    /**
     * 开机启动
     */
    @PostConstruct
    public void start() throws InterruptedException {
        // 绑定端口启动
        serverBootstrap.bind(nettyProperties.getPort()).sync();
        serverBootstrap.bind(nettyProperties.getPortSalve()).sync();
        log.info("Netty server started on port: {}, {}", nettyProperties.getPort(), nettyProperties.getPortSalve());
    }

    /**
     * 关闭线程池
     */
    @PreDestroy
    public void close() {
        log.info("close Netty server...");
        boosGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}