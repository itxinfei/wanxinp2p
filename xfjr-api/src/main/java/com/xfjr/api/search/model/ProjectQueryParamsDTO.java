package com.xfjr.api.search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "ProjectQueryParamsDTO", description = "标的检索条件")
public class ProjectQueryParamsDTO {

	@Schema(description = "标的标识")
	private Long id;

	@Schema(description = "数据标识列表")
	private Long [] ids;

	@Schema(description = "标的名称(分词匹配)")
	private String name;

	@Schema(description = "标的描述(分词匹配)")
	private String description;

	@Schema(description = "起止标的期限(单位:天)")
	private Integer startPeriod;

	@Schema(description = "起止标的期限(单位:天)")
	private Integer endPeriod;

	@Schema(description = "起止年化利率(投资人视图)")
	private BigDecimal startAnnualRate;

	@Schema(description = "起止年化利率(投资人视图)")
	private BigDecimal endAnnualRate;

	@Schema(description = "标的状态")
	private String projectStatus;

	@Schema(description = "可用状态")
	private Integer status;

	@Schema(description = "是否是债权出让标")
	private Integer isAssignment;

	@Schema(description = "全文关键词(名称+描述分词匹配)")
	private String keyword;

	@Schema(description = "标的金额下限")
	private BigDecimal amountMin;

	@Schema(description = "标的金额上限")
	private BigDecimal amountMax;

	@Schema(description = "还款方式")
	private String repaymentWay;
}
