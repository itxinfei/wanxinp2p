package com.xfjr.depository;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 存管代理
 *
 * @author 庞昭
 */
@SpringBootApplication(scanBasePackages = {"com.xfjr.depository"})
@EnableDiscoveryClient
@EnableTransactionManagement
public class DepositoryAgentService1 {
    public static void main(String[] args) {
        SpringApplication.run(DepositoryAgentService1.class, args);
    }
}
