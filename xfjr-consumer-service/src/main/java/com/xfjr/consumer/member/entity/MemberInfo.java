package com.xfjr.consumer.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户会员档案（对齐 p2p_consumer.member_info）：当前等级 + 累计出借金额 + 活跃度。
 */
@Data
@TableName("member_info")
public class MemberInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String consumerNo;
    /** 当前等级编码 */
    private String currentLevel;
    /** 累计出借金额 */
    private BigDecimal cumulativeLendAmount;
    /** 活跃度 */
    private Integer activeScore;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
