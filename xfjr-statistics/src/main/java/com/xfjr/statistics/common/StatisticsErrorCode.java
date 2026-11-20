package com.xfjr.statistics.common;

import com.xfjr.common.domain.ErrorCode;

/**
 * 统计分析服务错误码。
 * 前两位:服务标识(17=统计)  中间两位:模块标识  后两位:异常标识
 */
public enum StatisticsErrorCode implements ErrorCode {
    E_170101(170101, "指标查询失败"),
    E_170102(170102, "暂无统计数据"),
    E_170201(170201, "报表导出失败"),
    E_170301(170301, "指标聚合失败"),
    ;

    private int code;
    private String desc;

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    private StatisticsErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatisticsErrorCode setErrorCode(int code) {
        for (StatisticsErrorCode errorCode : StatisticsErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
