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
 * 投标信息表
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-15
 */
@Data
@Schema(name = "TenderDTO", description = "投标信息表")
public class TenderDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonSerialize(using= ToStringSerializer.class)
	@Schema(description = "主键")
	private Long id;

	@Schema(description = "请求流水号")
	private String requestNo;

	@Schema(description = "投标人用户编码")
	private String userNo;

	@Schema(description = "标的编码")
	private String projectNo;

	@Schema(description = "投标冻结金额")
	private BigDecimal amount;

	@Schema(description = "投标状态")
	private Integer tenderStatus;

	@Schema(description = "创建时间")
	private LocalDateTime createDate;

	@Schema(description = "应用编码")
	private String appCode;


}
