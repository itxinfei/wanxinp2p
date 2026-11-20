package com.xfjr.reconciliation.controller;

import com.xfjr.api.reconciliation.DiffHandleRequest;
import com.xfjr.api.reconciliation.ReconciliationAPI;
import com.xfjr.api.reconciliation.ReconciliationBatchDTO;
import com.xfjr.api.reconciliation.ReconciliationDiffDTO;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.reconciliation.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/l/reconciliations")
@Tag(name = "Reconciliation服务API")
public class ReconciliationController implements ReconciliationAPI {

    @Autowired
    private ReconciliationService reconciliationService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/health")
    public RestResponse<String> health() {
        return RestResponse.success("Reconciliation service alive");
    }

    @Operation(summary = "发起对账（上传银行对账单，可选平台流水）")
    @PostMapping
    public RestResponse<ReconciliationBatchDTO> createBatch(
            @RequestParam("bizDate") String bizDate,
            @RequestParam("bankStatement") MultipartFile bankStatement,
            @RequestParam(value = "platformStatement", required = false) MultipartFile platformStatement) {
        return RestResponse.success(reconciliationService.createBatch(bizDate, bankStatement, platformStatement));
    }

    @Operation(summary = "对账批次列表")
    @GetMapping
    public RestResponse<List<ReconciliationBatchDTO>> listBatches() {
        return RestResponse.success(reconciliationService.listBatches());
    }

    @Operation(summary = "批次差异明细")
    @GetMapping("/{batchNo}/diffs")
    public RestResponse<List<ReconciliationDiffDTO>> getDiffs(@PathVariable String batchNo) {
        return RestResponse.success(reconciliationService.getDiffs(batchNo));
    }

    @Operation(summary = "处理差异（人工挂账/调账）")
    @PutMapping("/diffs/{id}")
    public RestResponse<Void> handleDiff(@PathVariable Long id, @RequestBody DiffHandleRequest req) {
        reconciliationService.handleDiff(id, req);
        return RestResponse.success();
    }

    @Operation(summary = "导出差异报表(CSV)")
    @GetMapping("/{batchNo}/export")
    public RestResponse<String> export(@PathVariable String batchNo) {
        return RestResponse.success(reconciliationService.export(batchNo));
    }
}
