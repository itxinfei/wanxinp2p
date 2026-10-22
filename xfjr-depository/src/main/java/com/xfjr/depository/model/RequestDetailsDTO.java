package com.xfjr.depository.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 存管系统请求信息表
 * </p>
 *
 * @author zhupeiyuan
 * @since 2019-05-23
 */
@Data
@Schema(name = "RequestDetailsDTO", description = "存管系统请求信息表")
public class RequestDetailsDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	@Schema(description = "应用编码")
	private String appCode;

	private String requestNo;

	@Schema(description = "请求类型:1.用户信息、2.绑卡信息")
	private String serviceName;

	private String requestData;

	@Schema(description = "返回数据")
	private String responseData;

	@Schema(description = "执行结果")
	private Integer status;

	@Schema(description = "请求时间")
	private LocalDateTime createDate;

	@Schema(description = "执行返回时间")
	private LocalDateTime finishDate;

}
