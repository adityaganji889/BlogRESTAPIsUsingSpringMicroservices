package com.services.UserService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableAsync;

import com.services.UserService.config.JwtConfig;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(JwtConfig.class)
//@EnableAsync
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
