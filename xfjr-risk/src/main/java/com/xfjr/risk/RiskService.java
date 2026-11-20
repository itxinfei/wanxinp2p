package com.xfjr.risk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.xfjr.risk.mapper")
public class RiskService {
    public static void main(String[] args) {
        SpringApplication.run(RiskService.class, args);
    }
}
