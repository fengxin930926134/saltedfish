package com.fengx.saltedfish.config.netty;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(NettyServerBoot.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableNettyServer {
}