package com.xfjr.api.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <P>
 * 预授权处理请求信息
 * </p>
 */

@Data
@Schema(name = "UserAutoPreTransactionRequest", description = "预授权处理请求信息")
public class UserAutoPreTransactionRequest {

    @Schema(description = "冻结金额")
    private BigDecimal amount;

    @Schema(description = "请求流水号")
    private String requestNo;

    @Schema(description = "投资人用户编码")
    private String userNo;

    @Schema(description = "预处理业务类型")
    private String bizType;

    @Schema(description = "红包金额")
    private BigDecimal preMarketingAmount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "标的号")
    private String projectNo;

    @Schema(description = "债权出让流水号，购买债权时需填此参数")
    private String creditsaleRequestNo;

    @Schema(description = "预处理业务ID")
    private Long id;
}