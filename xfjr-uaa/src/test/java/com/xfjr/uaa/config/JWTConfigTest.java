package com.xfjr.uaa.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 签名密钥持久化测试：同一 key-path 多次构建必须得到同一份密钥，避免重启后旧令牌失效。
 */
class JWTConfigTest {

    @Test
    void 相同路径重复构建应返回同一份密钥(@TempDir Path tempDir) throws Exception {
        Path keyFile = tempDir.resolve("jwt-signing-key.json");
        JWTConfig first = new JWTConfig(keyFile.toString());
        RSAKey firstKey = selectRsaKey(first.jwkSource());

        JWTConfig second = new JWTConfig(keyFile.toString());
        RSAKey secondKey = selectRsaKey(second.jwkSource());

        assertThat(secondKey.getKeyID()).isEqualTo(firstKey.getKeyID());
        assertThat(secondKey.getModulus()).isEqualTo(firstKey.getModulus());
        assertThat(secondKey.getPrivateExponent()).isEqualTo(firstKey.getPrivateExponent());
        assertThat(keyFile).exists();
        // 落盘内容必须包含私钥参数 d，否则重启后无法验签旧令牌
        assertThat(Files.readString(keyFile)).contains("\"d\"");
    }

    @Test
    void 已有密钥文件应被复用而不重新生成(@TempDir Path tempDir) throws Exception {
        Path keyFile = tempDir.resolve("existing.json");
        RSAKey original = new JWTConfig(keyFile.toString()).jwkSource()
                .get(selector(), null).stream()
                .filter(k -> k instanceof RSAKey)
                .map(k -> (RSAKey) k)
                .findFirst()
                .orElseThrow();

        RSAKey reloaded = selectRsaKey(new JWTConfig(keyFile.toString()).jwkSource());

        assertThat(reloaded.getKeyID()).isEqualTo(original.getKeyID());
        assertThat(reloaded.getModulus()).isEqualTo(original.getModulus());
    }

    private RSAKey selectRsaKey(JWKSource<SecurityContext> source) throws JOSEException {
        List<JWK> keys = source.get(selector(), null);
        assertThat(keys).hasSize(1);
        assertThat(keys.get(0)).isInstanceOf(RSAKey.class);
        return (RSAKey) keys.get(0);
    }

    private JWKSelector selector() {
        return new JWKSelector(new JWKMatcher.Builder().build());
    }
}
