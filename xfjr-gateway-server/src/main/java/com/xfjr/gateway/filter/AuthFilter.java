package com.xfjr.gateway.filter;

import com.xfjr.api.account.model.LoginUser;
import com.xfjr.common.util.EncryptUtil;
import com.alibaba.fastjson2.JSON;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;

/**
 * 鉴权过滤器（Spring Cloud Gateway GlobalFilter，替代 ZuulFilter）：
 * 从 JWT 中解析当前登录用户信息，Base64 编码后以 jsonToken 参数透传给下游微服务。
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .map(this::buildJsonToken)
                .flatMap(jsonToken -> {
                    ServerHttpRequest request = addQueryParam(exchange.getRequest(), "jsonToken", jsonToken);
                    return chain.filter(exchange.mutate().request(request).build());
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private String buildJsonToken(Principal principal) {
        Jwt jwt = ((JwtAuthenticationToken) principal).getToken();
        LoginUser loginUser = new LoginUser();
        String username = jwt.getClaimAsString("user_name");
        loginUser.setUsername(username != null ? username : jwt.getSubject());
        loginUser.setMobile(jwt.getClaimAsString("mobile"));
        loginUser.setTenantId(jwt.getClaimAsString("tenant_id"));
        loginUser.setDepartmentId(jwt.getClaimAsString("department_id"));
        Object userAuthorities = jwt.getClaim("user_authorities");
        loginUser.setUserAuthorities(userAuthorities == null ? null : JSON.toJSONString(userAuthorities));
        Object payload = jwt.getClaim("payload");
        loginUser.setPayload(payload == null ? null : JSON.toJSONString(payload));
        loginUser.setClientId(jwt.getClaimAsString("azp"));
        return EncryptUtil.encodeUTF8StringBase64(JSON.toJSONString(loginUser));
    }

    private ServerHttpRequest addQueryParam(ServerHttpRequest request, String name, String value) {
        URI uri = UriComponentsBuilder.fromUri(request.getURI())
                .queryParam(name, value)
                .build()
                .toUri();
        return request.mutate().uri(uri).build();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
