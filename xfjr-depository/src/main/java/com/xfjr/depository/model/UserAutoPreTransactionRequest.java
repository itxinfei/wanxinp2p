package com.xfjr.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "UserAutoPreTransactionRequest", description = "预授权处理请求信息")
public class UserAutoPreTransactionRequest extends BaseRequest {

    @Schema(description = "出款人平台用户编号")
    private String userNo;

    @Schema(description = "预处理业务类型")
    private String bizType;

	@Schema(description = "冻结金额")
	private BigDecimal amount;

    @Schema(description = "红包金额")
    private BigDecimal preMarketingAmount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "标的号")
    private String projectNo;

    @Schema(description = "债权出让流水号，购买债权时需填此参数")
    private String creditsaleRequestNo;

}