package com.xfjr.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 风控名单（对齐 p2p_risk.risk_list 表）。
 * list_type: BLACK-黑名单 WHITE-白名单；dimension: MOBILE/ID_CARD/DEVICE/IP
 */
@TableName("risk_list")
public class RiskList implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String listType;
    private String dimension;
    private String value;
    private String reason;
    private String operator;
    private Integer status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getListType() { return listType; }
    public void setListType(String listType) { this.listType = listType; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
}
