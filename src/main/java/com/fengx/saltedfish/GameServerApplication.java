package com.fengx.saltedfish;

import com.fengx.saltedfish.config.netty.EnableNettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableNettyServer
@SpringBootApplication
public class GameServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameServerApplication.class, args);
	}

}
