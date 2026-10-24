package com.xfjr.api.risk;

import com.xfjr.common.domain.RestResponse;
import java.util.List;

/**
 * 风控服务契约（供网关/其他服务通过 Feign 调用）
 */
public interface RiskAPI {

    /**
     * 服务健康检查
     */
    RestResponse<String> health();

    /**
     * 触发风控决策（内部调用，发标/开户/提现等交易前触发）
     */
    RestResponse<RiskDecisionDTO> decide(RiskDecisionRequest req);

    /**
     * 按业务流水号查询决策记录
     */
    RestResponse<RiskDecisionDTO> getDecision(String bizNo);

    /**
     * 名单查询（type=BLACK/WHITE，可选）
     */
    RestResponse<List<RiskListDTO>> list(String type);

    /**
     * 新增名单（黑/白）
     */
    RestResponse<RiskListDTO> addList(RiskListRequest req);

    /**
     * 删除名单
     */
    RestResponse<Void> deleteList(Long id);
}
