package com.xfjr.depository.model;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 * 银行卡明细
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-15
 */
@Data
@Schema(name = "BankCardDetailsDTO", description = "银行卡明细")
public class BankCardDetailsDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主键")
	private Long id;

	@Schema(description = "银行卡ID")
	private Long bankCardId;

	@Schema(description = "账户变动类型")
	private Integer changeType;

	@Schema(description = "变动金额")
	private BigDecimal money;

	@Schema(description = "当前余额")
	private BigDecimal balance;

	@Schema(description = "账户变动时间")
	private LocalDateTime createDate;


}
