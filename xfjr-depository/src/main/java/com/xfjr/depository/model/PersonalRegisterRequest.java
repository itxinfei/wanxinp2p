package com.xfjr.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "PersonalRegisterRequest", description = "个人绑卡注册信息")
public class PersonalRegisterRequest extends BaseRequest {

	@Schema(description = "用户编码")
	private String userNo;

	@Schema(description = "真实姓名")
	private String fullname;

	@Schema(description = "身份证号")
	private String idNumber;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行名称")
	private String bankName;

	@Schema(description = "银行卡号")
	private String cardNumber;

	@Schema(description = "存管预留手机号")
	private String mobile;

	@Schema(description = "存管交易密码")
	private String password;

	@Schema(description = "角色")
	private String role;

	@Schema(description = "用户类型,个人or企业，预留")
	private Integer userType;

	@Schema(description = "授权列表")
	private String authList;

	@Schema(description = "页面回跳 URL")
	private String callbackUrl;

}
