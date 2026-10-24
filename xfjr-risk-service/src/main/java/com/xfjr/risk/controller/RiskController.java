package com.xfjr.risk.controller;

import com.xfjr.api.risk.RiskAPI;
import com.xfjr.api.risk.RiskDecisionDTO;
import com.xfjr.api.risk.RiskDecisionRequest;
import com.xfjr.api.risk.RiskListDTO;
import com.xfjr.api.risk.RiskListRequest;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.risk.service.RiskDecisionService;
import com.xfjr.risk.service.RiskListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/l/risk")
@Tag(name = "Risk服务API")
public class RiskController implements RiskAPI {

    @Autowired
    private RiskDecisionService riskDecisionService;
    @Autowired
    private RiskListService riskListService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/health")
    public RestResponse<String> health() {
        return RestResponse.success("Risk service alive");
    }

    @Operation(summary = "触发风控决策（内部调用）")
    @PostMapping("/decisions")
    public RestResponse<RiskDecisionDTO> decide(@RequestBody RiskDecisionRequest req) {
        return RestResponse.success(riskDecisionService.decide(req));
    }

    @Operation(summary = "按业务流水号查询决策记录")
    @GetMapping("/decisions/{bizNo}")
    public RestResponse<RiskDecisionDTO> getDecision(@PathVariable("bizNo") String bizNo) {
        return RestResponse.success(riskDecisionService.getByBizNo(bizNo));
    }

    @Operation(summary = "名单查询（type=BLACK/WHITE 可选）")
    @GetMapping("/lists")
    public RestResponse<List<RiskListDTO>> list(@RequestParam(value = "type", required = false) String type) {
        return RestResponse.success(riskListService.list(type));
    }

    @Operation(summary = "新增名单（黑/白）")
    @PostMapping("/lists")
    public RestResponse<RiskListDTO> addList(@RequestBody RiskListRequest req) {
        return RestResponse.success(riskListService.add(req));
    }

    @Operation(summary = "删除名单")
    @DeleteMapping("/lists/{id}")
    public RestResponse<Void> deleteList(@PathVariable("id") Long id) {
        riskListService.delete(id);
        return RestResponse.success();
    }
}
