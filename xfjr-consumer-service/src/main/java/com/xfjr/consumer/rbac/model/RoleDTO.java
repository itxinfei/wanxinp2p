package com.xfjr.consumer.rbac.model;

import java.util.List;
import lombok.Data;

/** 角色请求/响应（FR-131）。 */
@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String code;
    /** 关联权限 id 列表 */
    private List<Long> privilegeIds;
}
