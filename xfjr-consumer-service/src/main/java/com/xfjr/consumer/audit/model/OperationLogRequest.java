package com.xfjr.consumer.audit.model;

import lombok.Data;

/** 记录操作日志请求（FR-132）。 */
@Data
public class OperationLogRequest {
    /** 操作人 */
    private String operator;
    /** 动作类型 */
    private String action;
    /** 操作对象 */
    private String target;
    /** 结果 SUCCESS/FAIL */
    private String result;
    /** 来源 IP */
    private String ip;
}
