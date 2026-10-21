package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <P>
 * 银行卡注册信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-18
 */
@Data
@Schema(name = "BankCardRequest", description = "银行卡注册信息")
public class BankCardRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "真实姓名")
	private String fullname;

	@Schema(description = "身份证号")
	private String idNumber;

	@Schema(description = "银行预留手机号")
	private String mobile;

	@Schema(description = "用户类型,个人or企业，预留")
	private Integer userType;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行名称")
	private String bankName;

	@Schema(description = "银行卡号")
	private String cardNumber;

	@Schema(description = "银行卡密码")
	private String password;

	@Schema(description = "当前余额")
	private BigDecimal balance;
}
