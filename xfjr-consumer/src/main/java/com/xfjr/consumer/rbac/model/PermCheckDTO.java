package com.xfjr.consumer.rbac.model;

import lombok.Data;

/** 权限校验请求（FR-131 AC2）。 */
@Data
public class PermCheckDTO {
    private Long accountId;
    /** 权限编码，如 project:approve */
    private String code;
}
