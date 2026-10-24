package com.xfjr.consumer.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 角色-权限关系（对齐既有 role_privilege 表）。 */
@Data
@TableName("role_privilege")
public class RolePrivilege implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;
    @TableField("ROLE_ID")
    private Long roleId;
    @TableField("PRIVILEGE_ID")
    private Long privilegeId;
}
