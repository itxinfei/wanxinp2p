package com.xfjr.uaa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

/**
 * 客户端种子初始化（FR-129 AC1：客户端可动态增删改，初始清单落库）。
 *
 * <p>启动时校验 {@code oauth2_registered_client}，缺失的客户端写入；已存在的跳过，
 * 保证本地/测试环境从零初始化即可用，同时不覆盖运营期对客户端的修改。
 */
@Component
public class RegisteredClientSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RegisteredClientSeedRunner.class);

    private final RegisteredClientRepository registeredClientRepository;

    public RegisteredClientSeedRunner(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (RegisteredClient client : RegisteredClients.all()) {
            if (registeredClientRepository.findByClientId(client.getClientId()) == null) {
                registeredClientRepository.save(client);
                log.info("已初始化 OAuth2 客户端 {}", client.getClientId());
            }
        }
    }
}
