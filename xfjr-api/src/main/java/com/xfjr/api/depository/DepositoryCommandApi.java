package com.xfjr.api.depository;

import com.xfjr.api.depository.model.DepositoryCommandRequest;
import com.xfjr.api.depository.model.DepositoryCommandStatusDTO;
import com.xfjr.common.domain.RestResponse;

/**
 * 存管指令与异步通知契约（FR-134，供业务服务通过 Feign 调用）。
 */
public interface DepositoryCommandApi {

    /** 下发存管指令（内部）。 */
    RestResponse<DepositoryCommandStatusDTO> sendCommand(DepositoryCommandRequest request);

    /** 指令状态查询。 */
    RestResponse<DepositoryCommandStatusDTO> commandStatus(String requestNo);

    /** 补偿查询（指令超时无响应时兜底）。 */
    RestResponse<DepositoryCommandStatusDTO> compensate(String requestNo);
}
