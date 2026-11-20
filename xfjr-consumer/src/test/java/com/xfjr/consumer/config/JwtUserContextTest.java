package com.xfjr.consumer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.xfjr.api.account.model.LoginUser;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 当前用户（FR-130 AC4）")
class JwtUserContextTest {

    @Mock
    private Jwt jwt;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("从 JWT 声明提取当前用户各字段")
    void currentUser_from_jwt_claims() {
        when(jwt.getClaimAsString("user_name")).thenReturn("alice");
        when(jwt.getClaimAsString("mobile")).thenReturn("13800000000");
        when(jwt.getClaimAsString("tenant_id")).thenReturn("T1");
        when(jwt.getClaimAsString("department_id")).thenReturn("D1");
        when(jwt.getClaimAsString("azp")).thenReturn("xfjr-web-admin");
        when(jwt.getClaim("user_authorities")).thenReturn(List.of("ROLE_A"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        LoginUser user = JwtUserContext.currentUser();

        assertEquals("alice", user.getUsername());
        assertEquals("13800000000", user.getMobile());
        assertEquals("T1", user.getTenantId());
        assertEquals("D1", user.getDepartmentId());
        assertEquals("xfjr-web-admin", user.getClientId());
        assertTrue(user.getUserAuthorities() != null && user.getUserAuthorities().contains("ROLE_A"));
    }

    @Test
    @DisplayName("user_name 缺失 → 回退 subject")
    void currentUser_fallback_to_subject() {
        // getClaimAsString("user_name") 默认返回 null，触发回退
        when(jwt.getSubject()).thenReturn("bob");
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        LoginUser user = JwtUserContext.currentUser();

        assertEquals("bob", user.getUsername());
    }

    @Test
    @DisplayName("无认证 → 返回空 LoginUser（不抛错）")
    void currentUser_empty_when_no_auth() {
        LoginUser user = JwtUserContext.currentUser();

        assertNotNull(user);
        assertNull(user.getUsername());
    }
}
