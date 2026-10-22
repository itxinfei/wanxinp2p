package com.xfjr.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户余额明细表
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-15
 */
@Data
@Schema(name = "WithdrawDetailsDTO", description = "用户余额明细表")
public class WithdrawDetailsDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "请求流水号")
	private String requestNo;

	@Schema(description = "用户编码,生成唯一,用户在存管系统标识")
	private String userNo;

	@Schema(description = "金额")
	private BigDecimal amount;

	@Schema(description = "平台佣金")
	private BigDecimal commission;

	@Schema(description = "应用编码")
	private String appCode;


}
