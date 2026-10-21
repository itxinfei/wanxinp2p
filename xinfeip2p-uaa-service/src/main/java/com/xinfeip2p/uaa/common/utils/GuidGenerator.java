package com.xinfeip2p.uaa.common.utils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * 全局唯一ID生成器
 * <p>
 * 替代旧版 spring-security-oauth 的 RandomValueStringGenerator（该依赖已随
 * Spring Authorization Server 迁移移除），此处基于 JDK 安全随机数自行实现。
 * </p>
 */
public abstract class GuidGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成32位客户端密钥（等价于原 RandomValueStringGenerator(32) 的行为：
     * 取32字节安全随机数，以 URL 安全的 Base64 无填充编码）。
     */
    private static final int CLIENT_SECRET_BYTES = 32;

    /**
     * private constructor
     */
    private GuidGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String generateClientSecret() {
        byte[] bytes = new byte[CLIENT_SECRET_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
