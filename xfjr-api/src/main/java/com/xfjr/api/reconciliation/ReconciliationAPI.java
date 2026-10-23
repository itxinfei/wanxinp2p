package com.xfjr.api.reconciliation;

import com.xfjr.common.domain.RestResponse;

/**
 * 对账服务契约（供网关/其他服务通过 Feign 调用）
 */
public interface ReconciliationAPI {

    /**
     * 服务健康检查
     */
    RestResponse<String> health();
}
