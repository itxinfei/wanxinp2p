package com.xfjr.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户余额信息
 * </p>
 */
@Data
@Schema(name = "BalanceChangeDTO", description = "用户余额信息")
public class BalanceDetailsDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "用户标识")
	private Long consumerId;

	@Schema(description = "用户编码,生成唯一,用户在存管系统标识")
	private String userNo;

	@Schema(description = "账户变动类型.1.增加.2.减少.3.冻结.4解冻")
	private Integer changeType;

	@Schema(description = "冻结金额")
	private BigDecimal freezeAmount;

	@Schema(description = "可用余额")
	private BigDecimal balance;

	@Schema(description = "应用编码")
	private String appCode;

	@Schema(description = "账户变动时间")
	private LocalDateTime createDate;

}
