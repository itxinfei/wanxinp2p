package com.xfjr.risk.controller;

import com.xfjr.risk.service.RiskRuleService;
import com.xfjr.api.risk.RiskAPI;
import com.xfjr.common.domain.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Risk服务API")
public class RiskController implements RiskAPI {

    @Autowired
    private RiskRuleService riskRuleService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/l/risk/health")
    public RestResponse<String> health() {
        return RestResponse.success(riskRuleService.ping());
    }
}
