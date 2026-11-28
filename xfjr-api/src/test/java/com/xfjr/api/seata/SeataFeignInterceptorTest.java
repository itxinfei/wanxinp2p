package com.xfjr.api.seata;

import feign.RequestTemplate;
import org.apache.seata.core.context.RootContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Seata XID 透传测试：有全局事务时写入 TX_XID 头，无事务时不注入。
 */
class SeataFeignInterceptorTest {

    private final SeataFeignInterceptor interceptor = new SeataFeignInterceptor();

    @AfterEach
    void unbind() {
        RootContext.unbind();
    }

    @Test
    void 存在全局事务时应把XID写入请求头() {
        RootContext.bind("xid-001");

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).containsEntry(SeataFeignInterceptor.HEADER_XID, List.of("xid-001"));
        assertThat(SeataFeignInterceptor.HEADER_XID).isEqualTo(RootContext.KEY_XID);
    }

    @Test
    void 无全局事务时不应注入请求头() {
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).isEmpty();
    }
}
