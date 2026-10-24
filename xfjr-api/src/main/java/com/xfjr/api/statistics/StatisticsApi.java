package com.xfjr.api.statistics;

import com.xfjr.common.domain.RestResponse;

/**
 * 统计分析服务契约（供网关/其他服务通过 Feign 调用）。
 */
public interface StatisticsApi {

    /** 经营看板（最新 T+1 快照）。 */
    RestResponse<StatisticsOverviewDTO> overview();

    /** 风险看板（最新 T+1 快照）。 */
    RestResponse<StatisticsRiskDTO> risk();

    /** 经营看板报表导出（CSV）。 */
    RestResponse<String> exportOverview();

    /** 风险看板报表导出（CSV）。 */
    RestResponse<String> exportRisk();

    /** 统计聚合推送（T+1 管道入口，upsert）。 */
    RestResponse<Void> sync(StatisticsSyncRequest req);
}
