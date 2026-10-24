package com.xfjr.consumer.contract.controller;

import com.xfjr.api.contract.ContractApi;
import com.xfjr.api.contract.ContractDTO;
import com.xfjr.api.contract.ContractEvidenceDTO;
import com.xfjr.api.contract.CreateContractRequest;
import com.xfjr.api.contract.SignRequest;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.consumer.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 电子合同与存证（FR-133）。
 */
@RestController
@Slf4j
@Tag(name = "Contract", description = "电子合同与存证API")
public class ContractController implements ContractApi {

    @Autowired
    private ContractService contractService;

    @Override
    @Operation(summary = "生成合同（放款触发）")
    @PostMapping("/l/contracts")
    public RestResponse<List<ContractDTO>> create(@RequestBody CreateContractRequest req) {
        return RestResponse.success(contractService.create(req));
    }

    @Override
    @Operation(summary = "我的合同列表")
    @GetMapping("/my/contracts")
    public RestResponse<List<ContractDTO>> myContracts() {
        return RestResponse.success(contractService.myContracts());
    }

    @Override
    @Operation(summary = "合同详情/下载")
    @GetMapping("/my/contracts/{contractNo}")
    public RestResponse<ContractDTO> detail(@PathVariable("contractNo") String contractNo) {
        return RestResponse.success(contractService.detail(contractNo));
    }

    @Override
    @Operation(summary = "签署合同")
    @PostMapping("/l/contracts/{contractNo}/sign")
    public RestResponse<ContractDTO> sign(@PathVariable("contractNo") String contractNo, @RequestBody SignRequest req) {
        return RestResponse.success(contractService.sign(contractNo, req));
    }

    @Override
    @Operation(summary = "存证报告")
    @GetMapping("/l/contracts/{contractNo}/evidence")
    public RestResponse<List<ContractEvidenceDTO>> evidence(@PathVariable("contractNo") String contractNo) {
        return RestResponse.success(contractService.evidence(contractNo));
    }
}
