package com.xinfeip2p.api.transaction.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <P>
 * 投标信息
 * </p>
 */

@Data
@Schema(name = "ProjectInvestDTO", description = "用户投标信息")
public class ProjectInvestDTO {

    @Schema(description = "标的标识")
    private Long id;

    @Schema(description = "投标金额")
    private String amount;
}
