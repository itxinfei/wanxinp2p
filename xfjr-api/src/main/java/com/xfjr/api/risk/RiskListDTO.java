package com.xfjr.api.risk;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 风控名单（黑/白名单）。
 */
@Data
public class RiskListDTO {
    private Long id;
    /** BLACK-黑名单 WHITE-白名单 */
    private String listType;
    /** 维度：MOBILE-手机号 ID_CARD-身份证 DEVICE-设备 IP-IP */
    private String dimension;
    private String value;
    private String reason;
    /** 操作人 */
    private String operator;
    /** 状态 1生效 0失效 */
    private Integer status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
