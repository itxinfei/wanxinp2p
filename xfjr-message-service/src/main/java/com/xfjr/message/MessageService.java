package com.xfjr.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.xfjr.message.mapper")
public class MessageService {
    public static void main(String[] args) {
        SpringApplication.run(MessageService.class, args);
    }
}
