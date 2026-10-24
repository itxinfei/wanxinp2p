package com.xfjr.consumer.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 权限（对齐既有 privilege 表）。 */
@Data
@TableName("privilege")
public class Privilege implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;
    @TableField("NAME")
    private String name;
    @TableField("CODE")
    private String code;
    @TableField("PRIVILEGE_GROUP_ID")
    private Long privilegeGroupId;
}
