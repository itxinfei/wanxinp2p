package com.xinfeip2p.api.transaction.model;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <P>
 * 投标信息预览
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-06-25
 */
@Data
@Schema(name = "TenderOverviewDTO", description = "投标信息预览")
public class TenderOverviewDTO {

	@JsonSerialize(using= ToStringSerializer.class)
	@Schema(description = "主键")
    private Long id;

    @Schema(description = "投标人用户标识")
    private Long consumerId;

	@Schema(description = "投标人用户名")
	private String consumerUsername;

	@Schema(description = "投标冻结金额")
    private BigDecimal amount;

	@Schema(description = "投标方式")
    private String tenderWay = "手动出借";

	@Schema(description = "创建时间")
    private LocalDateTime createDate;

}
