package com.xfjr.uaa.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

/**
 * JWT 签名密钥配置（Spring Authorization Server 使用 JWKSource，替代 JwtAccessTokenConverter）。
 *
 * <p>密钥持久化到本地文件 {@code xfjr.jwt.key-path}（默认 {@code ${user.home}/.xfjr/jwt-signing-key.json}），
 * 避免每次启动重新生成导致已签发的 access token 全部验签失败。私钥只落本地磁盘，
 * 不进入代码仓库；生产环境应改为 KMS / Apollo 注入专用密钥材料。
 */
@Configuration
public class JWTConfig {

    private static final Logger log = LoggerFactory.getLogger(JWTConfig.class);

    /**
     * 签名密钥文件路径，可由 Apollo 命名空间覆盖。
     */
    private final String keyPath;

    public JWTConfig(@Value("${xfjr.jwt.key-path:${user.home}/.xfjr/jwt-signing-key.json}") String keyPath) {
        this.keyPath = keyPath;
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = loadOrCreateRsaKey(keyPath);
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * 读取已有签名密钥；文件不存在时生成新密钥对并落盘。
     *
     * @param path 密钥文件路径（JWK JSON，含私钥）
     * @return RSA 签名密钥
     */
    private RSAKey loadOrCreateRsaKey(String path) {
        Path keyFile = Paths.get(path);
        if (Files.isRegularFile(keyFile)) {
            return loadFrom(keyFile);
        }
        RSAKey rsaKey = generateRsa();
        persistQuietly(keyFile, new JWKSet(rsaKey));
        return rsaKey;
    }

    /**
     * 从 JWK JSON 文件解析签名密钥，解析失败直接报错（配置/文件错误应尽早暴露）。
     */
    private RSAKey loadFrom(Path keyFile) {
        try {
            String json = Files.readString(keyFile, StandardCharsets.UTF_8);
            List<JWK> keys = JWKSet.parse(json).getKeys();
            if (keys.size() != 1 || !(keys.get(0) instanceof RSAKey rsaKey)) {
                throw new IllegalArgumentException("密钥文件必须包含且仅包含一个 RSA JWK: " + keyFile);
            }
            log.info("已加载 JWT 签名密钥，kid={}", rsaKey.getKeyID());
            return rsaKey;
        } catch (Exception ex) {
            throw new IllegalStateException("加载 JWT 签名密钥失败: " + keyFile, ex);
        }
    }

    /**
     * 将密钥写入本地文件；写盘失败只告警并退回内存模式（不阻塞启动）。
     */
    private void persistQuietly(Path keyFile, JWKSet jwkSet) {
        try {
            Path parent = keyFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(keyFile.toAbsolutePath(), jwkSet.toString(false),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.warn("JWT 签名密钥不存在，已生成并写入 {}（请妥善保管，勿提交到代码仓库）", keyFile.toAbsolutePath());
        } catch (Exception ex) {
            log.error("JWT 签名密钥写入失败，本次启动将使用内存密钥（重启后旧令牌会失效）: {}", keyFile.toAbsolutePath(), ex);
        }
    }

    private static RSAKey generateRsa() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("生成RSA密钥失败", ex);
        }
    }
}
