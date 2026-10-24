package com.xfjr.consumer.audit.controller;

import com.xfjr.common.domain.RestResponse;
import com.xfjr.consumer.audit.entity.OperationLog;
import com.xfjr.consumer.audit.model.OperationLogRequest;
import com.xfjr.consumer.audit.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志与审计（FR-132）。
 */
@RestController
@RequestMapping("/api/v1/operation-logs")
@Tag(name = "OperationLog", description = "操作日志与审计API")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @Operation(summary = "追加操作日志（仅追加，不可修改/删除）")
    @PostMapping
    public RestResponse<OperationLog> append(@RequestBody OperationLogRequest req) {
        return RestResponse.success(
                operationLogService.append(req.getOperator(), req.getAction(), req.getTarget(), req.getResult(), req.getIp()));
    }

    @Operation(summary = "按操作人/时间范围/动作检索")
    @GetMapping
    public RestResponse<List<OperationLog>> query(
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return RestResponse.success(operationLogService.query(operator, action, start, end));
    }

    @Operation(summary = "校验哈希链完整性")
    @GetMapping("/verify")
    public RestResponse<Boolean> verify() {
        return RestResponse.success(operationLogService.verifyChain());
    }
}
