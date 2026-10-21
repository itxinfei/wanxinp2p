package cn.itcast.xinfeidepository.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 * 银行用户银行卡信息
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-15
 */
@Data
@Schema(name = "BankCardDTO", description = "银行用户银行卡信息")
public class BankCardDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主键")
	private Long id;

	@Schema(description = "用户标识")
	private Long userId;

	@Schema(description = "银行编码")
	private String bankCode;

	@Schema(description = "银行名称")
	private String bankName;

	@Schema(description = "银行卡号")
	private String cardNumber;

	@Schema(description = "银行卡密码")
	private String password;


}
