package com.xfjr.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ConsumerRequest", description = "平台c端用户开户信息")
public class ConsumerRequest {

	@Schema(description = "id")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "真实姓名")
	private String fullname;

	@Schema(description = "身份证号")
	private String idNumber;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行卡号")
	private String cardNumber;

	@Schema(description = "手机号")
	private String mobile;

	@Schema(description = "角色")
	private String role;

	@Schema(description = "请求流水号")
	private String requestNo;

	@Schema(description = "用户编号")
	private String userNo;

	@Schema(description = "页面回跳 URL")
	private String callbackUrl;

}
