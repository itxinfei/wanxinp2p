package com.xfjr.depository.controller;

import com.xfjr.api.depository.DepositoryCommandApi;
import com.xfjr.api.depository.model.DepositoryCommandRequest;
import com.xfjr.api.depository.model.DepositoryCommandStatusDTO;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.depository.service.DepositoryCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 存管指令与异步通知（FR-134）。
 */
@RestController
@Tag(name = "depository-command", description = "存管指令与异步通知(FR-134)")
public class DepositoryCommandController implements DepositoryCommandApi {

    @Autowired
    private DepositoryCommandService commandService;

    @Override
    @Operation(summary = "下发存管指令（内部）")
    @PostMapping("/l/depository/commands")
    public RestResponse<DepositoryCommandStatusDTO> sendCommand(@RequestBody DepositoryCommandRequest request) {
        return RestResponse.success(commandService.sendCommand(request));
    }

    @Override
    @Operation(summary = "指令状态查询")
    @GetMapping("/l/depository/commands/{requestNo}")
    public RestResponse<DepositoryCommandStatusDTO> commandStatus(@PathVariable("requestNo") String requestNo) {
        return RestResponse.success(commandService.commandStatus(requestNo));
    }

    @Override
    @Operation(summary = "补偿查询")
    @PostMapping("/l/depository/commands/{requestNo}/compensate")
    public RestResponse<DepositoryCommandStatusDTO> compensate(@PathVariable("requestNo") String requestNo) {
        return RestResponse.success(commandService.compensate(requestNo));
    }
}
