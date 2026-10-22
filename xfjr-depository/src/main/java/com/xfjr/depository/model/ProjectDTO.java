package com.xfjr.depository.model;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 标的信息表
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-15
 */
@Data
@Schema(name = "ProjectDTO", description = "标的信息表")
public class ProjectDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonSerialize(using= ToStringSerializer.class)
	@Schema(description = "主键")
	private Long id;

	@Schema(description = "发标人用户编码")
	private String userNo;

	@Schema(description = "标的编码")
	private String projectNo;

	@Schema(description = "标的名称")
	private String name;

	@Schema(description = "标的描述")
	private String description;

	@Schema(description = "标的类型")
	private Integer type;

	@Schema(description = "标的期限(单位:天)")
	private Integer period;

	@Schema(description = "年化利率(5%只要传5)")
	private Integer annualRate;

	@Schema(description = "还款方式")
	private Integer repaymentWay;

	@Schema(description = "募集金额")
	private BigDecimal amount;

	@Schema(description = "标的状态")
	private Integer projectStatus;

	@Schema(description = "创建时间")
	private LocalDateTime createDate;

	@Schema(description = "应用编码")
	private String appCode;

	@Schema(description = "是否是债权出让标")
	private Integer isDebentureSale;

	@Schema(description = "投标信息标识(转让来源)")
	private String sourceTenderRequestNo;

	@Schema(description = "原始标的编码(冗余)")
	private String rootProjectNo;

	@Schema(description = "债权转让 请求流水号 ,债权转让时ProjectNO为空")
	private String debentureSaleRequestNo;


}
