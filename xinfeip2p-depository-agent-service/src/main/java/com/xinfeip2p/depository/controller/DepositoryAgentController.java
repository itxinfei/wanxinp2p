package com.xinfeip2p.depository.controller;

import com.xinfeip2p.api.consumer.model.ConsumerRequest;
import com.xinfeip2p.api.depository.DepositoryAgentApi;
import com.xinfeip2p.api.depository.GatewayRequest;
import com.xinfeip2p.api.depository.model.*;
import com.xinfeip2p.api.transaction.model.ModifyProjectStatusDTO;
import com.xinfeip2p.api.transaction.model.ProjectDTO;
import com.xinfeip2p.common.domain.RestResponse;
import com.xinfeip2p.depository.service.DepositoryRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 存管代理服务
 */
@Tag(name = "depository-agent", description = "存管代理服务")
@RestController
public class DepositoryAgentController implements DepositoryAgentApi {

    @Autowired
    private DepositoryRecordService depositoryRecordService;

    @Override
    @Operation(summary = "生成开户请求数据")
    @PostMapping("/l/consumers")
    public RestResponse<GatewayRequest> createConsumer(@RequestBody ConsumerRequest  consumerRequest) {
        return RestResponse.success(depositoryRecordService.createConsumer(consumerRequest));
    }

    @Override
    @Operation(summary = "向存管系统发送标的信息")
    @PostMapping("/l/createProject")
    public RestResponse<String> createProject(@RequestBody ProjectDTO projectDTO) {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse =
                depositoryRecordService.createProject(projectDTO);
        RestResponse<String> restResponse=new RestResponse<String>();
        restResponse.setResult(depositoryResponse.getRespData().getRespCode());
        restResponse.setMsg(depositoryResponse.getRespData().getRespMsg());
        return restResponse;
    }

    @Override
    @Operation(summary = "预授权处理")
    @PostMapping("/l/user-auto-pre-transaction")
    public RestResponse<String> userAutoPreTransaction(@RequestBody UserAutoPreTransactionRequest userAutoPreTransactionRequest) {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = depositoryRecordService.userAutoPreTransaction(userAutoPreTransactionRequest);
        return getRestResponse(depositoryResponse);
    }

    @Override
    @Operation(summary = "审核标的满标放款")
    @PostMapping("l/confirm-loan")
    public RestResponse<String> confirmLoan(@RequestBody LoanRequest loanRequest) {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse =depositoryRecordService.confirmLoan(loanRequest);
        return getRestResponse(depositoryResponse);
    }

    @Override
    @Operation(summary = "修改标的状态")
    @PostMapping("l/modify-project-status")
    public RestResponse<String> modifyProjectStatus(@RequestBody ModifyProjectStatusDTO modifyProjectStatusDTO) {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponseDTO=depositoryRecordService.modifyProjectStatus(modifyProjectStatusDTO);
        return getRestResponse(depositoryResponseDTO);
    }

    @Override
    @Operation(summary = "确认还款")
    @PostMapping("l/confirm-repayment")
    public RestResponse<String> confirmRepayment(@RequestBody RepaymentRequest repaymentRequest) {
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponseDTO=depositoryRecordService.confirmRepayment(repaymentRequest);
        return getRestResponse(depositoryResponseDTO);
    }

    /**
       * 统一处理响应信息
        * @param depositoryResponse
        * @return
     */
    private RestResponse<String>  getRestResponse(DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse) {
        final RestResponse<String> restResponse = new RestResponse<>();
        restResponse.setResult(depositoryResponse.getRespData().getRespCode());
        restResponse.setMsg(depositoryResponse.getRespData().getRespMsg());
        return restResponse;
    }
}