package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <P>
 * 封装提现请求数据
 * </p>
 *
 * @author zhangdongxu
 * @since 2019/6/5
 */
@Data
@Schema(description = "用户提现请求信息")
public class WithdrawRequest extends BaseRequest {

	@Schema(description = "标识")
	private Long id;

	@Schema(description = "用户编号")
	private String userNo;

	@Schema(description = "提现金额")
	private BigDecimal amount;

	@Schema(description = "平台佣金")
	private BigDecimal commission;

	@Schema(description = "页面回调URL")
	private String callbackUrl;

	@Schema(description = "银行卡号")
	private String cardNumber;

	@Schema(description = "银行预留手机号")
	private String mobile;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行名称")
	private String bankName;
}
