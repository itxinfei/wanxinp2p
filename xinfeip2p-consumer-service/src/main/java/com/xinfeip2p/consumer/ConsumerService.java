package com.xinfeip2p.consumer;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户中心微服务
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.xinfeip2p.consumer.mapper")
@EnableFeignClients(basePackages = {"com.xinfeip2p.consumer.agent"})
public class ConsumerService {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerService.class, args);
    }
}