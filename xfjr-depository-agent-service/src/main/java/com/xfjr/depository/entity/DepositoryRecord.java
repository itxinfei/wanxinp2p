package com.xfjr.depository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@Accessors(chain = true)
public class DepositoryRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId("ID")
    private Long id;

    /**
     * 请求流水号
     */
    @TableField("REQUEST_NO")
    private String requestNo;

    /**
     * 请求类型:1.用户信息(新增、编辑)、2.绑卡信息
     */
    @TableField("REQUEST_TYPE")
    private String requestType;

    /**
     * 业务实体类型
     */
    @TableField("OBJECT_TYPE")
    private String objectType;

    /**
     * 关联业务实体标识
     */
    @TableField("OBJECT_ID")
    private Long objectId;

    /**
     * 请求时间
     */
    @TableField("CREATE_DATE")
    private LocalDateTime createDate;

    /**
     * 是否是同步调用
     */
    @TableField("IS_SYN")
    private Integer isSyn;

    /**
     * 数据同步状态
     */
    @TableField("REQUEST_STATUS")
    private Integer requestStatus;

    /**
     * 消息确认时间
     */
    @TableField("CONFIRM_DATE")
    private LocalDateTime confirmDate;

	/**
	 * 返回数据
	 */
	@TableField("RESPONSE_DATA")
	private String responseData;

	/**
	 * 存管接口名（FR-134）
	 */
	@TableField("SERVICE_NAME")
	private String serviceName;

	/**
	 * 指令方向 IN/OUT（FR-134）
	 */
	@TableField("DIRECTION")
	private String direction;

	/**
	 * 指令金额（FR-134）
	 */
	@TableField("AMOUNT")
	private java.math.BigDecimal amount;

	/**
	 * 请求报文（FR-134，防篡改留痕）
	 */
	@TableField("REQUEST_PAYLOAD")
	private String requestPayload;

	/**
	 * 重试次数（FR-134）
	 */
	@TableField("RETRY_COUNT")
	private Integer retryCount;

	/**
	 * 更新时间（FR-134）
	 */
	@TableField("UPDATE_DATE")
	private LocalDateTime updateDate;

	/**
	 * 业务服务标识（FR-134，回调区分业务）
	 */
	@TableField("SERVICE_CODE")
	private String serviceCode;

	/**
	 * 指令状态 INIT/PROCESSING/SUCCESS/FAIL/CANCELED（FR-134）
	 */
	@TableField("STATUS")
	private String status;

	/**
	 * 错误信息（FR-134）
	 */
	@TableField("ERROR_MESSAGE")
	private String errorMessage;

	public DepositoryRecord() {
	}

	public DepositoryRecord(String requestNo, String requestType, String objectType, Long objectId) {
		this.requestNo = requestNo;
		this.requestType = requestType;
		this.objectType = objectType;
		this.objectId = objectId;
	}
}
