package com.xfjr.statistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.xfjr.statistics.mapper")
public class StatisticsService {
    public static void main(String[] args) {
        SpringApplication.run(StatisticsService.class, args);
    }
}
