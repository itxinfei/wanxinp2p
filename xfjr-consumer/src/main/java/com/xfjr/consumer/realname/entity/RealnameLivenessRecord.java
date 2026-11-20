package com.xfjr.consumer.realname.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 活体/人脸核验记录（FR-135 AC4：分数与结果可追溯；不存原始人脸图像）。
 */
@Data
@TableName("realname_liveness_record")
public class RealnameLivenessRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String consumerNo;
    /** 活体检测分数(0~1) */
    private BigDecimal livenessScore;
    /** 人脸比对分数(0~1) */
    private BigDecimal faceMatchScore;
    /** 活体是否通过 1是 0否 */
    private Integer livenessPassed;
    /** 攻击类型：PHOTO/REPLAY/SCREEN/MASK（AC2） */
    private String attackType;
    /** 结论：PASSED/REVIEW/REJECTED */
    private String status;
    /** 重试次数 */
    private Integer retryCount;
    private String message;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
