package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "RechargeRequest", description = "用户充值请求信息")
public class RechargeRequest extends BaseRequest {

	@Schema(description = "标识")
	private Long id;

	@Schema(description = "用户编号")
	private String userNo;

	@Schema(description = "充值金额")
	private BigDecimal amount;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行名称")
	private String bankName;

	@Schema(description = "银行卡号")
	private String cardNumber;

	@Schema(description = "银行预留手机号")
	private String mobile;

	@Schema(description = "交易密码")
	private String password;

	@Schema(description = "页面回跳 URL")
	private String callbackUrl;
}