package com.xinfeip2p.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 统一账户服务
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.xinfeip2p.account.mapper")
public class AccountService {
    public static void main(String[] args) {
        SpringApplication.run(AccountService.class, args);
    }
}
