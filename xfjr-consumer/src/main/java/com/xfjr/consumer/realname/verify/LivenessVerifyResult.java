package com.xfjr.consumer.realname.verify;

import java.math.BigDecimal;
import lombok.Data;

/** 活体/人脸核验结果（算法服务返回）。 */
@Data
public class LivenessVerifyResult {
    /** 活体检测分数(0~1) */
    private BigDecimal livenessScore;
    /** 人脸比对分数(0~1) */
    private BigDecimal faceMatchScore;
    /** 活体是否通过 */
    private boolean livenessPassed;
    /** 是否检测到攻击（翻拍/照片/屏幕） */
    private boolean attackDetected;
    /** 攻击类型：PHOTO/REPLAY/SCREEN/MASK */
    private String attackType;
    private String message;
}
