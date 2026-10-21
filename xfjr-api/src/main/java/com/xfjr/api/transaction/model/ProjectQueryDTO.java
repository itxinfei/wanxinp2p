package com.xfjr.api.transaction.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <P>
 * 标的信息查询对象
 * </p>
 */
@Data
@Schema(name = "ProjectQueryDTO", description = "标的信息查询对象")
public class ProjectQueryDTO {

    @Schema(description = "标的类型")
    private String type;

    @Schema(description = "名称")
	private String name;

    @Schema(description = "起止标的期限(单位:天)")
    private Integer startPeriod;

    @Schema(description = "起止标的期限(单位:天)")
    private Integer endPeriod;

    @Schema(description = "起止年化利率(投资人视图)")
    private BigDecimal startAnnualRate;

    @Schema(description = "起止年化利率(投资人视图)")
    private BigDecimal endAnnualRate;

    @Schema(description = "年化利率(借款人视图)")
    private BigDecimal borrowerAnnualRate;

    @Schema(description = "还款方式")
    private String repaymentWay;

    @Schema(description = "标的状态")
    private String projectStatus;

}
