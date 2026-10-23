package com.xfjr.reconciliation.controller;

import com.xfjr.reconciliation.service.ReconciliationBatchService;
import com.xfjr.api.reconciliation.ReconciliationAPI;
import com.xfjr.common.domain.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Reconciliation服务API")
public class ReconciliationController implements ReconciliationAPI {

    @Autowired
    private ReconciliationBatchService reconciliationBatchService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/l/reconciliation/health")
    public RestResponse<String> health() {
        return RestResponse.success(reconciliationBatchService.ping());
    }
}
