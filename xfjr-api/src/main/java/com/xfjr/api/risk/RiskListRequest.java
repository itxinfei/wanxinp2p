package com.xfjr.api.risk;

import lombok.Data;

/**
 * 风控名单维护请求（新增）。
 */
@Data
public class RiskListRequest {
    /** BLACK-黑名单 WHITE-白名单（必填） */
    private String listType;
    /** 维度：MOBILE/ID_CARD/DEVICE/IP（必填） */
    private String dimension;
    /** 名单值（必填） */
    private String value;
    /** 命中/加入原因 */
    private String reason;
    /** 操作人 */
    private String operator;
}
