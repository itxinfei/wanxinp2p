package com.xfjr.consumer.rbac.model;

import java.util.List;
import lombok.Data;

/** 账号-角色请求/响应（FR-131）。 */
@Data
public class AccountDTO {
    private Long accountId;
    /** 关联角色 id 列表 */
    private List<Long> roleIds;
}
