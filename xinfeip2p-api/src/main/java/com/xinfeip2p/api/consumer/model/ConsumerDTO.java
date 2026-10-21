package com.xinfeip2p.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;


@Data
@Schema(name = "ConsumerDTO", description = "平台c端用户信息")
public class ConsumerDTO {

	@Schema(description = "用户id")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "真实姓名")
	private String fullname;

	@Schema(description = "身份证号")
	private String idNumber;

	@Schema(description = "用户编码，用户在存管系统标识，不允许重复")
	private String userNo;

	@Schema(description = "手机号")
	private String mobile;

	@Schema(description = "用户类型,个人or企业，预留")
	private String userType;

	@Schema(description = "角色")
	private String role;

	@Schema(description = "存管授权列表")
	private String authList;

	@Schema(description = "是否已绑定银行卡")
	private Integer isBindCard;

	@Schema(description = "启用状态")
	private Integer status;

	@Schema(description = "可贷额度")
	private BigDecimal loanAmount;

}