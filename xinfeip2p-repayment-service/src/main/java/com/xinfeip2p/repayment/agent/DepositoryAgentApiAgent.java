package com.xinfeip2p.repayment.agent;


import com.xinfeip2p.api.depository.model.RepaymentRequest;
import com.xinfeip2p.api.depository.model.UserAutoPreTransactionRequest;
import com.xinfeip2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "depository-agent-service")
public interface DepositoryAgentApiAgent {
    @PostMapping("/depository-agent/l/user-auto-pre-transaction")
    RestResponse<String> userAutoPreTransaction(
            UserAutoPreTransactionRequest userAutoPreTransactionRequest);

    @PostMapping("/depository-agent/l/confirm-repayment")
    RestResponse<String> confirmRepayment(RepaymentRequest repaymentRequest);
}
