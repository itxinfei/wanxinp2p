package com.xfjr.transaction.agent;

import com.xfjr.api.depository.model.LoanRequest;
import com.xfjr.api.depository.model.UserAutoPreTransactionRequest;
import com.xfjr.api.transaction.model.ModifyProjectStatusDTO;
import com.xfjr.api.transaction.model.ProjectDTO;
import com.xfjr.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "depository-agent-service")
public interface DepositoryAgentApiAgent {

    @PostMapping(value = "/depository-agent/l/createProject")
    public RestResponse<String> createProject(ProjectDTO projectDTO);

    @PostMapping("/depository-agent/l/user-auto-pre-transaction")
    RestResponse<String> userAutoPreTransaction(
            UserAutoPreTransactionRequest userAutoPreTransactionRequest);

    @PostMapping("/depository-agent/l/confirm-loan")
    RestResponse<String> confirmLoan(LoanRequest loanRequest);

    @PostMapping("/depository-agent/l/modify-project-status")
    RestResponse<String> modifyProjectStatus(ModifyProjectStatusDTO modifyProjectStatusDTO);
}
