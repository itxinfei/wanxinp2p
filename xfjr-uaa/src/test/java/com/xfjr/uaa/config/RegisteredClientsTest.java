package com.xfjr.uaa.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 客户端清单口径守卫（FR-129 AC1/AC2）。
 */
class RegisteredClientsTest {

    @Test
    void allClientSecretsAreBcrypt() {
        for (RegisteredClient client : RegisteredClients.all()) {
            assertThat(client.getClientSecret())
                    .as("客户端 %s 密钥必须为 BCrypt 密文", client.getClientId())
                    .startsWith("{bcrypt}");
            assertThat(client.getClientSecret()).doesNotContain("{noop}");
        }
    }

    @Test
    void threeClientsPresentWithExpectedGrants() {
        List<RegisteredClient> clients = RegisteredClients.all();
        assertThat(clients).extracting(RegisteredClient::getClientId)
                .containsExactlyInAnyOrder("xfjr-web-admin", "xfjr-web-h5", "xfjr-internal");

        RegisteredClient admin = clients.stream()
                .filter(c -> "xfjr-web-admin".equals(c.getClientId())).findFirst().orElseThrow();
        assertThat(admin.getAuthorizationGrantTypes())
                .contains(AuthorizationServer.PASSWORD_GRANT_TYPE, AuthorizationGrantType.AUTHORIZATION_CODE);
        assertThat(admin.getScopes()).contains("read", "write");

        RegisteredClient internal = clients.stream()
                .filter(c -> "xfjr-internal".equals(c.getClientId())).findFirst().orElseThrow();
        assertThat(internal.getAuthorizationGrantTypes())
                .contains(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .doesNotContain(AuthorizationServer.PASSWORD_GRANT_TYPE);
        assertThat(internal.getRedirectUris()).isEmpty();
    }

    @Test
    void tokenTtlUnified() {
        assertThat(RegisteredClients.tokenSettings().getAccessTokenTimeToLive()).hasSeconds(7200);
        assertThat(RegisteredClients.tokenSettings().getRefreshTokenTimeToLive()).hasSeconds(259200);
    }
}
