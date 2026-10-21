package cn.itcast.xinfeidepository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 银行卡检索信息
 * </p>
 *
 * @author zhupeiyuan@itcast.cn
 * @since 2019-05-18
 */
@Data
@Schema(name = "BankCardQuery", description = "银行卡检索信息")
public class BankCardQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "真实姓名")
	private String fullname;

	@Schema(description = "身份证号")
	private String idNumber;

	@Schema(description = "银行预留手机号")
	private String mobile;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行卡号")
	private String cardNumber;
}
