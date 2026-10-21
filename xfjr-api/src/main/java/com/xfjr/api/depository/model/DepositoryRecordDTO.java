package com.xfjr.api.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 存管交易记录表
 * </p>
 *
 * @author wuzhao@itcast.cn
 * @since 2019-05-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DepositoryRecordDTO对象", description="存管交易记录表")
public class DepositoryRecordDTO implements Serializable {


    @Schema(description = "主键")
    private Long id;

    @Schema(description = "请求流水号")
    private String requestNo;

    @Schema(description = "请求类型:1.用户信息(新增、编辑)、2.绑卡信息")
    private String requestType;

    @Schema(description = "业务实体类型")
    private String objectType;

    @Schema(description = "关联业务实体标识")
    private Long objectId;

    @Schema(description = "请求时间")
    private LocalDateTime createDate;

    @Schema(description = "是否是同步调用")
    private Integer isSyn;

    @Schema(description = "数据同步状态")
    private Integer requestStatus;

    @Schema(description = "消息确认时间")
    private LocalDateTime confirmDate;

	@Schema(description = "返回数据")
	private String responseData;


}
