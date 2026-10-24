package com.xfjr.consumer.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/** 菜单（对齐既有 menu 表；PRIVILEGE_CODE 绑定权限）。 */
@Data
@TableName("menu")
public class Menu implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;
    @TableField("PARENT_ID")
    private Long parentId;
    @TableField("TITLE")
    private String title;
    @TableField("URL")
    private String url;
    @TableField("ICON")
    private String icon;
    @TableField("SORT")
    private Integer sort;
    @TableField("`COMMENT`")
    private String comment;
    @TableField("STATUS")
    private Integer status;
    @TableField("PRIVILEGE_CODE")
    private String privilegeCode;
}
