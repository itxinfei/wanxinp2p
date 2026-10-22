package com.xfjr.depository.model;

import com.xfjr.depository.common.constant.AuditStatusCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "PersonalRegisterResponse", description = "个人绑卡注册返回信息")
public class PersonalRegisterResponse extends BaseResponse {

	@Schema(description = "银行代码")
	private String bankCode;

	@Schema(description = "银行名称")
	private String bankName;

	@Schema(description = "审核状态")
	private String auditStatus;

	@Override
	public void setSuccess() {
		super.setSuccess();
		this.setAuditStatus(AuditStatusCode.PASSED.getCode());
	}

	@Override
	public void setFailure() {
		super.setFailure();
		this.setAuditStatus(AuditStatusCode.REFUSED.getCode());
	}
}
