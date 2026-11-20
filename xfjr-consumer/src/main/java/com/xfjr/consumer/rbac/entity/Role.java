package com.xfjr.consumer.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 角色（对齐既有 role 表）。 */
@Data
@TableName("role")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;
    @com.baomidou.mybatisplus.annotation.TableField("NAME")
    private String name;
    @com.baomidou.mybatisplus.annotation.TableField("CODE")
    private String code;
}
