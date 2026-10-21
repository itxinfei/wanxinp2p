package com.xinfeip2p.api.account.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(name = "AccountDTO", description = "账户信息")
public class AccountDTO {

	@Schema(description = "标识")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "手机号")
	private String mobile;

	@Schema(description = "账号状态")
	private Integer status;

	@Schema(description = " 域(c：c端用户；b：b端用户)")
	private String domain;

}
