package com.xinfeip2p.api.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 封装账户登录信息
 * </p>
 */
@Data
@Schema(name = "AccountLoginDTO", description = "账户登录信息")
public class AccountLoginDTO {

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "手机号")
	private String mobile;

	@Schema(description = "密码")
	private String password;

	@Schema(description = "域(c：c端用户；b：b端用户)")
	private String domain;

}
