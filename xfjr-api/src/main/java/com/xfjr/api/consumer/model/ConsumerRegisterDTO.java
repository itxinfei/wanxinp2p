package com.xfjr.api.consumer.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(name = "ConsumerRegisterDTO", description = "用户注册信息")
public class ConsumerRegisterDTO {

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "手机号")
	private String mobile;

	@Schema(description = "密码")
	private String password;

	@Schema(description = "用户角色.B借款人or I投资人")
	private String role;

	@Schema(description = "验证码key")
	private String key;

}
