package com.xfjr.api.seata;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.seata.core.context.RootContext;

/**
 * Feign 调用透传 Seata 全局事务 XID（I-04：资金操作纳入分布式事务的基础设施）。
 *
 * <p>Seata starter 只自动解析入站 HTTP 请求的 {@code TX_XID}（见
 * {@code SeataHttpAutoConfiguration}），出站 Feign 需要显式把当前线程绑定的 XID
 * 写入请求头，下游服务的 AT 分支事务才会挂到同一个全局事务下，失败才能一并回滚。
 *
 * <p>未开启全局事务时 {@code RootContext.getXID()} 为空，拦截器不做任何写入，调用行为不变。
 */
public class SeataFeignInterceptor implements RequestInterceptor {

    /**
     * 与 {@code RootContext.KEY_XID} 保持一致的请求头名。
     */
    public static final String HEADER_XID = "TX_XID";

    @Override
    public void apply(RequestTemplate template) {
        String xid = RootContext.getXID();
        if (xid != null && !xid.isEmpty()) {
            template.header(HEADER_XID, xid);
        }
    }
}
