package com.xfjr.api.risk;

import com.xfjr.common.domain.RestResponse;

/**
 * 风控服务契约（供网关/其他服务通过 Feign 调用）
 */
public interface RiskAPI {

    /**
     * 服务健康检查
     */
    RestResponse<String> health();
}
