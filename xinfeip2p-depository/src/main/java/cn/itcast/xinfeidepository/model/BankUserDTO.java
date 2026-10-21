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
 * 银行用户信息表
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-15
 */
@Data
@Schema(name = "BankUserDTO", description = "银行用户信息表")
public class BankUserDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主键")
	private Long id;

	@Schema(description = "真实姓名")
	private String fullname;

	@Schema(description = "身份证号")
	private String idNumber;

	@Schema(description = "银行预留手机号")
	private String mobile;

	@Schema(description = "用户类型,个人or企业，预留")
	private Integer userType;


}
