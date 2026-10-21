package com.xfjr.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "BankCardDTO", description = "银行卡信息")
public class BankCardDTO {

	@Schema(description = "标识")
	private Long id;

	@Schema(description = "用户标识")
	private Long consumerId;

	@Schema(description = "用户实名")
	private String fullName;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行名称")
	private String bankName;

	@Schema(description = "银行卡号")
	private String cardNumber;

	@Schema(description = "银行预留手机号")
	private String mobile;

	@Schema(description = "可用状态")
	private Integer status;

}
