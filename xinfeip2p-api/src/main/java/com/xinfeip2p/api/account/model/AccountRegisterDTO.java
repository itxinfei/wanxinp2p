package com.xinfeip2p.api.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(name = "AccountRegisterDTO", description = "账户注册信息")
public class AccountRegisterDTO {

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "手机号")
	private String mobile;

	@Schema(description = "密码")
	private String password;

}
