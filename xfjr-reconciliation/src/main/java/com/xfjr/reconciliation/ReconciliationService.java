package com.xfjr.reconciliation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.xfjr.reconciliation.mapper")
public class ReconciliationService {
    public static void main(String[] args) {
        SpringApplication.run(ReconciliationService.class, args);
    }
}
