package com.xfjr.statistics.controller;

import com.xfjr.api.statistics.StatisticsApi;
import com.xfjr.api.statistics.StatisticsOverviewDTO;
import com.xfjr.api.statistics.StatisticsRiskDTO;
import com.xfjr.api.statistics.StatisticsSyncRequest;
import com.xfjr.common.domain.RestResponse;
import com.xfjr.statistics.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计分析（FR-128）。
 */
@RestController
@RequestMapping("/l/statistics")
@Tag(name = "Statistics服务API")
public class StatisticsController implements StatisticsApi {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "服务健康检查")
    @GetMapping("/health")
    public RestResponse<String> health() {
        return RestResponse.success("Statistics service alive");
    }

    @Operation(summary = "经营看板")
    @GetMapping("/overview")
    public RestResponse<StatisticsOverviewDTO> overview() {
        return RestResponse.success(dashboardService.overview());
    }

    @Operation(summary = "风险看板")
    @GetMapping("/risk")
    public RestResponse<StatisticsRiskDTO> risk() {
        return RestResponse.success(dashboardService.risk());
    }

    @Operation(summary = "经营看板报表导出(CSV)")
    @GetMapping("/overview/export")
    public RestResponse<String> exportOverview() {
        return RestResponse.success(dashboardService.exportOverview());
    }

    @Operation(summary = "风险看板报表导出(CSV)")
    @GetMapping("/risk/export")
    public RestResponse<String> exportRisk() {
        return RestResponse.success(dashboardService.exportRisk());
    }

    @Operation(summary = "统计聚合推送(T+1 管道入口)")
    @PostMapping("/sync")
    public RestResponse<Void> sync(@RequestBody StatisticsSyncRequest req) {
        dashboardService.sync(req);
        return RestResponse.success();
    }
}
