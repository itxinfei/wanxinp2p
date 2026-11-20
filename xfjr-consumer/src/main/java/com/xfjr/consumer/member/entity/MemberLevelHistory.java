package com.xfjr.consumer.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 等级变更历史（对齐 p2p_consumer.member_level_history），支撑 FR-127 AC1“记录变更历史”。
 */
@Data
@TableName("member_level_history")
public class MemberLevelHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String consumerNo;
    private String fromLevel;
    private String toLevel;
    private String changeReason;
    private LocalDateTime createDate;
}
