package com.xfjr.repayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 还款
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableTransactionManagement
@EnableFeignClients(basePackages = {"com.xfjr.repayment.agent"})
public class RepaymentService {
    public static void main(String[] args) {
        SpringApplication.run(RepaymentService.class, args);
    }
}
