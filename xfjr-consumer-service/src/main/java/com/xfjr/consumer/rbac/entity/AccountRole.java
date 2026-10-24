package com.xfjr.consumer.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 账号-角色关系（对齐既有 account_role 表）。 */
@Data
@TableName("account_role")
public class AccountRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;
    @TableField("ACCOUNT_ID")
    private Long accountId;
    @TableField("ROLE_ID")
    private Long roleId;
}
