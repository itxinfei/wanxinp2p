package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 存管用户绑定银行卡信息
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-15
 */
@Data
@Schema(name = "DepositoryBankCardDTO", description = "存管用户绑定银行卡信息")
public class DepositoryBankCardDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主键")
	private Long id;

	@Schema(description = "用户标识")
	private Long userId;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行卡号")
	private String cardNumber;

	@Schema(description = "银行预留手机号")
	private String mobile;

	@Schema(description = "应用编码")
	private String appCode;

	@Schema(description = "请求流水号")
	private String requestNo;


}
