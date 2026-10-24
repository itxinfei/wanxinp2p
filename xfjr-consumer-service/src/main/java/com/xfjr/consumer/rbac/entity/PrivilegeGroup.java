package com.xfjr.consumer.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 权限组（对齐既有 privilege_group 表）。 */
@Data
@TableName("privilege_group")
public class PrivilegeGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;
    @TableField("PARENT_ID")
    private Long parentId;
    @TableField("NAME")
    private String name;
}
