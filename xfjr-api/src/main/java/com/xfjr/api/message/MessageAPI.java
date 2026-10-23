package com.xfjr.api.message;

import com.xfjr.common.domain.RestResponse;

/**
 * 消息服务契约（供网关/其他服务通过 Feign 调用）
 */
public interface MessageAPI {

    /**
     * 服务健康检查
     */
    RestResponse<String> health();
}
